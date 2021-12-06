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
public class InviteRequestParameters {
    private Group invitedTo;
    private Player invitedBy;
    private Player invitedPlayer;

    @SneakyThrows
    public static InviteRequestParameters of(String requestParametersJson) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module());
        return mapper.readValue(requestParametersJson, InviteRequestParameters.class);
    }
}
