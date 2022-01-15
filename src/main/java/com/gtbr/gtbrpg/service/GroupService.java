package com.gtbr.gtbrpg.service;

import com.gtbr.gtbrpg.domain.configurations.GroupLimitConfiguration;
import com.gtbr.gtbrpg.domain.configurations.requests.InviteRequestParameters;
import com.gtbr.gtbrpg.domain.configurations.requests.SubscribeRequestParameters;
import com.gtbr.gtbrpg.domain.configurations.requests.UpdateGroupRequestParameters;
import com.gtbr.gtbrpg.domain.configurations.requests.utils.RequestBuildParameterUtil;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final PlayerService playerService;
    private final ConfigurationRepository configurationRepository;
    private final GroupPlayerRepository groupPlayerRepository;
    private final GroupRepository groupRepository;
    private final RequestService requestService;

    public GroupPlayerDto createGroup(Map<String, Object> parameters, String discordIdCreator) {
        ParameterUtils.validadeCreateGroupParameters(parameters);
        Player creator = playerService.getPlayerByDiscordId(discordIdCreator);

        Optional<GroupPlayer> groupPlayer = groupPlayerRepository.findActualGroupByPlayerId(creator.getPlayerId());

        if (groupPlayer.isPresent() && !creator.isAdmin())
            throw new RuntimeException("Voce só ter um grupo ativo.");

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
                .joinedAt(LocalDateTime.now())
                .build());

        if (!Objects.equals(leader.getDiscordId(), creator.getDiscordId()))
            groupPlayerRepository.save(GroupPlayer.builder()
                    .group(group)
                    .player(leader)
                    .joinedAt(LocalDateTime.now())
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
                InviteRequestParameters inviteRequestParameters = RequestBuildParameterUtil.of(request.getRequestParameter(), InviteRequestParameters.class);
                GroupPlayerDto groupPlayerDto = findGroupById(inviteRequestParameters.getInvitedTo().getGroupId());
                if (groupPlayerDto.playerList().size() >= groupPlayerDto.group().getSize())
                    throw new RuntimeException("Grupo cheio!");
                if (!findAllGroupsByPlayerId(inviteRequestParameters.getInvitedPlayer().getPlayerId()).isEmpty())
                    throw new RuntimeException("Nao e possivel estar em mais de um grupo por vez, para aceitar essa requisicao saia do outro grupo");

                GroupPlayer groupPlayer = groupPlayerRepository.save(GroupPlayer.builder()
                        .group(inviteRequestParameters.getInvitedTo())
                        .player(inviteRequestParameters.getInvitedPlayer())
                        .joinedAt(LocalDateTime.now())
                        .build());
                List<Player> players = groupPlayerDto.playerList();
                players.add(groupPlayer.getPlayer());

                yield new GroupPlayerDto(groupPlayerDto.group(), players, null);
            }
            case SUBSCRIBE -> {
                SubscribeRequestParameters subscribeRequestParameters = RequestBuildParameterUtil.of(request.getRequestParameter(), SubscribeRequestParameters.class);
                GroupPlayerDto groupPlayerDto = findGroupById(subscribeRequestParameters.getSubscribedGroup().getGroupId());
                if (groupPlayerDto.playerList().size() >= groupPlayerDto.group().getSize())
                    throw new RuntimeException("Grupo cheio!");
                if (!findAllGroupsByPlayerId(subscribeRequestParameters.getIssuer().getPlayerId()).isEmpty())
                    throw new RuntimeException("Nao e possivel estar em mais de um grupo por vez, para aceitar essa requisicao saia do outro grupo");

                GroupPlayer groupPlayer = groupPlayerRepository.save(GroupPlayer.builder().group(subscribeRequestParameters.getSubscribedGroup()).player(subscribeRequestParameters.getIssuer()).build());
                List<Player> players = groupPlayerDto.playerList();
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

    public void sendInvite(List<User> mentionedUsers, User author) {
        if (mentionedUsers.size() > getGroupSizeLimit())
            throw new RuntimeException("Esta convidando players de mais, o maximo permitido e: " + getGroupSizeLimit());

        Group group = groupPlayerRepository.findActualGroupByPlayerId(playerService.getPlayerByDiscordId(author.getId()).getPlayerId()).orElseThrow(() -> {
            throw new RuntimeException("Você não está em nenhum grupo.");
        }).getGroup();
        GroupPlayerDto groupPlayerDto = findGroupById(group.getGroupId());
        Player inviter = playerService.getPlayerByDiscordId(author.getId());
        MessageUtil.hasPermission(inviter, List.of(groupPlayerDto.group().getLeader()), true);

        if ((mentionedUsers.size() + groupPlayerDto.playerList().size()) > getGroupSizeLimit())
            throw new RuntimeException(String.format("O numero de convites ultrapassa o limite permitido, seu grupo ja tem %s com esses novos convites ficariam em %s e o maximo permitido e: %s", groupPlayerDto.playerList().size(), mentionedUsers.size() + groupPlayerDto.playerList().size(), getGroupSizeLimit()));

        mentionedUsers.forEach(user -> {
            requestInviteGroup(group.getGroupId(), playerService.getPlayerByDiscordId(user.getId()), inviter);
        });
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
        GroupPlayer groupPlayer = groupPlayerRepository.findActualGroupByPlayerId(playerService.getPlayerByDiscordId(discordId).getPlayerId()).orElseThrow(() -> {
            throw new RuntimeException("Voce nao esta em nenhum grupo!");
        });

        return findGroupById(groupPlayer.getGroup().getGroupId());
    }

    public GroupPlayer leaveGroup(String discordId) {
        Player player = playerService.getPlayerByDiscordId(discordId);
        GroupPlayer groupPlayer = groupPlayerRepository.findActualGroupByPlayerId(player.getPlayerId()).orElseThrow(() -> {
            throw new RuntimeException("Voce não está em nenhum grupo!");
        });

        groupPlayer.setLeaveAt(LocalDateTime.now());

        return groupPlayerRepository.save(groupPlayer);
    }

    public GroupPlayerDto closeGroup(String discordId) {
        Player player = playerService.getPlayerByDiscordId(discordId);
        GroupPlayerDto groupPlayerDto = findGroupById(groupPlayerRepository.findActualGroupByPlayerId(player.getPlayerId()).orElseThrow(() -> {
            throw new RuntimeException("Voce não está em nenhum grupo!");
        }).getGroup().getGroupId());

        if (player.isAdmin() || Objects.equals(player.getDiscordId(), groupPlayerDto.group().getLeader().getDiscordId())) {
            groupPlayerDto.playerList().forEach(playerInGroup -> {
                leaveGroup(playerInGroup.getDiscordId());
            });
            Group group = groupPlayerDto.group();

            group.setClosedAt(LocalDateTime.now());

            groupRepository.save(group);

            return groupPlayerDto;
        }

        throw new RuntimeException("Você não tem permissão para fechar este grupo, fale com o lider ou o admin para fecha-lo");
    }

    public GroupPlayerDto updateGroup(String discordId, Map<String, Object> paramatersMap) {
        GroupPlayerDto groupPlayerDto = findGroupByPlayer(discordId);
        Player player = playerService.getPlayerByDiscordId(discordId);

        if (Objects.equals(groupPlayerDto.group().getLeader().getDiscordId(), discordId) || player.isAdmin()) {
            Group groupByParameters = ParameterUtils.buildGroupByParameters(paramatersMap);
            if (paramatersMap.containsKey("lider"))
                groupByParameters.setLeader(playerService.getPlayerByDiscordId((String) paramatersMap.get("lider")));

            Group group = groupRepository.save(groupMergeData(groupPlayerDto.group(), groupByParameters, player));

            Request request = Request.builder()
                    .requestParameter(new JSONObject(new UpdateGroupRequestParameters(groupPlayerDto.group(), paramatersMap, groupByParameters, group, player)).toString())
                    .requestType(RequestType.UPDATE)
                    .processed(true)
                    .requestedAt(LocalDateTime.now())
                    .processedAt(LocalDateTime.now())
                    .reviewerObservation(String.format("The player %s has requested this updated", player.getName()))
                    .requestStatus(RequestStatus.ACEITA)
                    .build();

            requestService.register(request);

            return findGroupById(group.getGroupId());
        }

        throw new RuntimeException("Você não tem permissão para alterar este grupo");
    }

    private Group groupMergeData(Group group, Group groupByParameters, Player player) {
        if (Objects.nonNull(groupByParameters.getName())) group.setName(groupByParameters.getName());
        if (Objects.nonNull(groupByParameters.getDescription()))
            group.setDescription(groupByParameters.getDescription());
        if (Objects.nonNull(groupByParameters.getSize()) && player.isAdmin())
            group.setSize(groupByParameters.getSize());
        if (Objects.nonNull(groupByParameters.getThumbnail())) group.setThumbnail(groupByParameters.getThumbnail());
        if (Objects.nonNull(groupByParameters.getLeader())) group.setLeader(groupByParameters.getLeader());

        return group;
    }

    public Group addRoleId(Integer groupId, String roleId) {
        GroupPlayerDto groupPlayerDto = findGroupById(groupId);
        Group group = groupPlayerDto.group();

        group.setRoleId(roleId);

        return groupRepository.save(group);
    }
}
