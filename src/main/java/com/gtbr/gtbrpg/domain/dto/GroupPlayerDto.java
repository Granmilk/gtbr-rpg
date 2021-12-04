package com.gtbr.gtbrpg.domain.dto;

import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.Player;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupPlayerDto(Group group, List<Player> playerList) {
}
