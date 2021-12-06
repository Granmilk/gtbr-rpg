package com.gtbr.gtbrpg.domain.configurations.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
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

    @SneakyThrows
    public static SubscribeRequestParameters of(String requestParametersJson) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module());
        return mapper.readValue(requestParametersJson, SubscribeRequestParameters.class);
    }
}
