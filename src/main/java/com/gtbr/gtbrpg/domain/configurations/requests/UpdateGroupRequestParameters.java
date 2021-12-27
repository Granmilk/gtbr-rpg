package com.gtbr.gtbrpg.domain.configurations.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.Player;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupRequestParameters {
    private Group originalGroup;
    private Map<String, Object> parameters;
    private Group buildedGroupByParameters;
    private Group updatedGroup;
    private Player issuer;
}
