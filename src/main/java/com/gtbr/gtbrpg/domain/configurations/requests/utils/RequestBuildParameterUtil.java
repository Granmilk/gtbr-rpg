package com.gtbr.gtbrpg.domain.configurations.requests.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import lombok.SneakyThrows;

public class RequestBuildParameterUtil {

    @SneakyThrows
    public static <T> T of(String requestParametersJson, Class<T> valueType) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module());
        return mapper.readValue(requestParametersJson, valueType);
    }

}
