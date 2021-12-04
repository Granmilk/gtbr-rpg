package com.gtbr.gtbrpg.domain.configurations;

import lombok.Builder;
import lombok.Getter;
import org.json.JSONObject;

@Builder
@Getter
public class GroupLimitConfiguration {
    private Integer sizeLimit;

    public static GroupLimitConfiguration of(String rawConfig) {
        JSONObject jsonObject = new JSONObject(rawConfig);

        return GroupLimitConfiguration.builder().sizeLimit(jsonObject.getInt("size")).build();
    }
}
