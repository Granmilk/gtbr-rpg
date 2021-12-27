package com.gtbr.gtbrpg.domain.configurations.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.gtbr.gtbrpg.domain.entity.Player;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestParameter {
    private Player player;
    private String message;
    private LocalDateTime scheduledAt;
    private LocalDateTime sendAt;
}
