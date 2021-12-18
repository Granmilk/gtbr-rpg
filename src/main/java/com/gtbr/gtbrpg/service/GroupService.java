package com.gtbr.gtbrpg.service;

import com.gtbr.gtbrpg.domain.configurations.GroupLimitConfiguration;
import com.gtbr.gtbrpg.domain.configurations.requests.InviteRequestParameters;
import com.gtbr.gtbrpg.domain.configurations.requests.SubscribeRequestParameters;
import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.GroupPlayer;
import com.gtbr.gtbrpg.domain.entity.Player;
import com.gtbr.gtbrpg.domain.entity.Request;
import com.gtbr.gtbrpg.domain.enums.RequestStatus;
import com.gtbr.gtbrpg.domain.enums.RequestType;
import com.gtbr.gtbrpg.repository.ConfigurationRepository;
import com.gtbr.gtbrpg.repository.GroupPlayerRepository;
import com.gtbr.gtbrpg.repository.GroupRepository;
import com.gtbr.gtbrpg.util.Constants;
import com.gtbr.gtbrpg.util.MessageUtil;
import com.gtbr.gtbrpg.util.ParameterUtils;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final PlayerService playerService;
    private final ConfigurationRepository configurationRepository;
    private final GroupPlayerRepository groupPlayerRepository;
    private final GroupRepository groupRepository;
    private final RequestService requestService;
    private final ScheduledService scheduledService;

    public GroupPlayerDto createGroup(Map<String, Object> parameters, String discordIdCreator) {
        ParameterUtils.validadeCreateGroupParameters(parameters);
        Player creator = playerService.getPlayerByDiscordId(discordIdCreator);
        Player leader;
        try {
            leader = playerService.getPlayerByDiscordId((String) parameters.get("lider"));
        } catch (RuntimeException e) {
            leader = creator;
        }
        groupRepository.findByName((String) parameters.get("nome")).ifPresent(group -> {
            throw new RuntimeException("Ja existe um grupo com este nome.");
        });
        Group group = Group.builder()
                .createdAt(LocalDateTime.now())
                .creator(creator)
                .description((String) parameters.get("descricao"))
                .leader(leader)
                .name((String) parameters.get("nome"))
                .thumbnail((String) parameters.get("thumbnail"))
                .size(GroupLimitConfiguration.of(configurationRepository.findById(Constants.GROUP_LIMIT_CONFIG)
                        .orElseThrow(() -> {
                            throw new RuntimeException("Configuracao de tamanho do grupo vazia");
                        }).getParameters()).getSizeLimit())
                .build();

        group = groupRepository.save(group);
        groupPlayerRepository.save(GroupPlayer.builder()
                .group(group)
                .player(creator)
                .since(LocalDateTime.now())
                .build());

        if (!Objects.equals(leader.getDiscordId(), creator.getDiscordId()))
            groupPlayerRepository.save(GroupPlayer.builder()
                    .group(group)
                    .player(leader)
                    .since(LocalDateTime.now())
                    .build());


        return new GroupPlayerDto(group, !Objects.equals(leader.getDiscordId(), creator.getDiscordId())
                ? List.of(creator, leader)
                : List.of(creator), LocalDateTime.now());
    }

    public GroupPlayerDto findGroupById(Integer groupId) {
        List<GroupPlayer> groupPlayerList = groupPlayerRepository.findByGroupId(groupId);
        return new GroupPlayerDto(groupPlayerList.get(0).getGroup(),
                groupPlayerList.stream().map(GroupPlayer::getPlayer).collect(Collectors.toList()),
                LocalDateTime.now());
    }

    public Request requestJoinGroup(String groupId, String issuerId) {
        GroupPlayerDto group = findGroupById(MessageUtil.hasRequestObservation(groupId)
                ? Integer.valueOf(groupId.trim().split(" ")[0])
                : Integer.valueOf(groupId));

        return requestService.register(Request.builder()
                .processIfStatus(RequestStatus.SEM_RESPOSTA)
                .playerObservation(MessageUtil.hasRequestObservation(groupId)
                        ? groupId.trim().split(" ")[1]
                        : "")
                .requestType(RequestType.SUBSCRIBE)
                .requestStatus(RequestStatus.SEM_RESPOSTA)
                .requestParameter(new JSONObject(SubscribeRequestParameters.builder()
                        .subscribedGroup(group.group())
                        .issuer(playerService.getPlayerByDiscordId(issuerId))
                        .build()).toString())
                .build());
    }

    public GroupPlayerDto acceptGroupInvite(Request request) {
        return switch (request.getRequestType()) {
            case INVITE -> {
                InviteRequestParameters inviteRequestParameters = InviteRequestParameters.of(request.getRequestParameter());
                GroupPlayerDto groupPlayerDto = findGroupById(inviteRequestParameters.getInvitedTo().getGroupId());
                if (groupPlayerDto.playerList().size() >= groupPlayerDto.group().getSize())
                    throw new RuntimeException("Grupo cheio!");
                if (!findAllGroupsByPlayerId(inviteRequestParameters.getInvitedPlayer().getPlayerId()).isEmpty())
                    throw new RuntimeException("Nao e possivel estar em mais de um grupo por vez, para aceitar essa requisicao saia do outro grupo");

                GroupPlayer groupPlayer = groupPlayerRepository.save(GroupPlayer.builder().group(inviteRequestParameters.getInvitedTo()).player(inviteRequestParameters.getInvitedPlayer()).build());
                List<Player> players = List.copyOf(groupPlayerDto.playerList());
                players.add(groupPlayer.getPlayer());

                yield new GroupPlayerDto(groupPlayerDto.group(), players, null);
            }
            case SUBSCRIBE -> {
                SubscribeRequestParameters subscribeRequestParameters = SubscribeRequestParameters.of(request.getRequestParameter());
                GroupPlayerDto groupPlayerDto = findGroupById(subscribeRequestParameters.getSubscribedGroup().getGroupId());
                if (groupPlayerDto.playerList().size() >= groupPlayerDto.group().getSize())
                    throw new RuntimeException("Grupo cheio!");
                if (!findAllGroupsByPlayerId(subscribeRequestParameters.getIssuer().getPlayerId()).isEmpty())
                    throw new RuntimeException("Nao e possivel estar em mais de um grupo por vez, para aceitar essa requisicao saia do outro grupo");

                GroupPlayer groupPlayer = groupPlayerRepository.save(GroupPlayer.builder().group(subscribeRequestParameters.getSubscribedGroup()).player(subscribeRequestParameters.getIssuer()).build());
                List<Player> players = List.copyOf(groupPlayerDto.playerList());
                players.add(groupPlayer.getPlayer());

                yield new GroupPlayerDto(groupPlayerDto.group(), players, null);
            }
            default -> {
                throw new RuntimeException("Tipo de requisicao nao reconhecido");
            }
        };
    }

    private List<GroupPlayer> findAllGroupsByPlayerId(Integer playerId) {
        return groupPlayerRepository.findAllGroupsByPlayerId(playerId);
    }

    public void sendInvite(Integer groupId, List<User> mentionedUsers, User author) {
        if (mentionedUsers.size() > getGroupSizeLimit())
            throw new RuntimeException("Esta convidando players de mais, o maximo permitido e: " + getGroupSizeLimit());

        GroupPlayerDto groupPlayerDto = findGroupById(groupId);
        Player inviter = playerService.getPlayerByDiscordId(author.getId());
        MessageUtil.hasPermission(inviter, List.of(groupPlayerDto.group().getLeader()), true);

        if ((mentionedUsers.size() + groupPlayerDto.playerList().size()) > getGroupSizeLimit())
            throw new RuntimeException(String.format("O numero de convites ultrapassa o limite permitido, seu grupo ja tem %s com esses novos convites ficariam em %s e o maximo permitido e: %s", groupPlayerDto.playerList().size(), mentionedUsers.size() + groupPlayerDto.playerList().size(), getGroupSizeLimit()));

        mentionedUsers.forEach(user -> {
            requestInviteGroup(groupId, playerService.getPlayerByDiscordId(user.getId()), inviter);
        });
        scheduledService.processRequests();
    }

    public Request requestInviteGroup(Integer groupId, Player invitedPlayer, Player invitedBy) {
        GroupPlayerDto group = findGroupById(groupId);

        return requestService.register(Request.builder()
                .processIfStatus(RequestStatus.SEM_RESPOSTA)
                .playerObservation("")
                .requestType(RequestType.INVITE)
                .requestStatus(RequestStatus.SEM_RESPOSTA)
                .requestParameter(new JSONObject(InviteRequestParameters.builder()
                        .invitedBy(invitedBy)
                        .invitedTo(group.group())
                        .invitedPlayer(invitedPlayer)
                        .build()).toString())
                .build());
    }

    private Integer getGroupSizeLimit() {
        return GroupLimitConfiguration.of(configurationRepository.findById(Constants.GROUP_LIMIT_CONFIG)
                .orElseThrow(() -> {
                    throw new RuntimeException("Configuracao de tamanho do grupo vazia");
                }).getParameters()).getSizeLimit();

    }

    public GroupPlayerDto findGroupByPlayer(String discordId) {
        return findGroupById(groupPlayerRepository.findAllGroupsByPlayerId(playerService.getPlayerByDiscordId(discordId).getPlayerId()).get(0).getGroup().getGroupId());

    }
}
