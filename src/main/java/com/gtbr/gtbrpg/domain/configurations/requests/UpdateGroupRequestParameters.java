package com.gtbr.gtbrpg.domain.configurations.requests;

import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
