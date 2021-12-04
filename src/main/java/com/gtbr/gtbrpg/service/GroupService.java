package com.gtbr.gtbrpg.service;

import com.gtbr.gtbrpg.domain.configurations.GroupLimitConfiguration;
import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.GroupPlayer;
import com.gtbr.gtbrpg.domain.entity.Player;
import com.gtbr.gtbrpg.repository.ConfigurationRepository;
import com.gtbr.gtbrpg.repository.GroupPlayerRepository;
import com.gtbr.gtbrpg.repository.GroupRepository;
import com.gtbr.gtbrpg.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final PlayerService playerService;
    private final ConfigurationRepository configurationRepository;
    private final GroupPlayerRepository groupPlayerRepository;
    private final GroupRepository groupRepository;

    public GroupPlayerDto createGroup(Map<String, Object> parameters, String discordIdCreator) {
        Player creator = playerService.getPlayerByDiscordId(discordIdCreator);
        Player leader;
        try {
            leader = playerService.getPlayerByDiscordId((String) parameters.get("lider"));
        } catch (RuntimeException e){
            leader = creator;
        }


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
                : List.of(creator));
    }
}
