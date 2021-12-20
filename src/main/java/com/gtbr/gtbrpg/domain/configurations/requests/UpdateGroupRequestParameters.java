package com.gtbr.gtbrpg.domain.configurations.requests;

import lombok.SneakyThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.Player;

import java.util.Map;

public record UpdateGroupRequestParameters(Group originalGroup, Map<String, Object> parameters,
                                           Group buildedGroupByParameters, Group updatedGroup, Player issuer) {

    @SneakyThrows
    public static UpdateGroupRequestParameters of(String requestParametersJson){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module());
        return mapper.readValue(requestParametersJson, UpdateGroupRequestParameters.class);
    }
}
