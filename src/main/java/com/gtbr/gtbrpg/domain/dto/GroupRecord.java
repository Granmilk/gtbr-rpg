package com.gtbr.gtbrpg.domain.dto;

import com.gtbr.gtbrpg.domain.entity.Player;

import java.time.LocalDateTime;

public record GroupRecord(Integer groupId, String name, String thumbnail,
                          String description, Player leader, Player creator,
                          Integer size, LocalDateTime createdAt, LocalDateTime closedAt) {
}
