package com.gtbr.gtbrpg.domain.configurations.requests;

import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.Player;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequestParameters {
    private Group subscribedGroup;
    private Player issuer;
}
