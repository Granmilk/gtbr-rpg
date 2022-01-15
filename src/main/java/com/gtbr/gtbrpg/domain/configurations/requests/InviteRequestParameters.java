package com.gtbr.gtbrpg.domain.configurations.requests;

import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteRequestParameters {
    private Group invitedTo;
    private Player invitedBy;
    private Player invitedPlayer;
}
