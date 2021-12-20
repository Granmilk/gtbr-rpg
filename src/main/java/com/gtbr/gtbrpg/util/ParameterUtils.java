package com.gtbr.gtbrpg.util;

import com.gtbr.gtbrpg.domain.entity.Group;

import java.util.Map;

public class ParameterUtils {
    public static void validadeCreateGroupParameters(Map<String, Object> parameters) {
        if (!parameters.containsKey("nome")) throw new RuntimeException("Parametro `nome` e obrigatorio");
        if (!parameters.containsKey("descricao")) throw new RuntimeException("Parametro `descricao` e obrigatorio");
    }

    public static Group buildGroupByParameters(Map<String, Object> paramatersMap) {
        return Group.builder()
                .name(paramatersMap.containsKey("nome") ? (String) paramatersMap.get("nome") : null)
                .description(paramatersMap.containsKey("descricao") ? (String) paramatersMap.get("descricao") : null)
                .thumbnail(paramatersMap.containsKey("thumbnail") ? (String) paramatersMap.get("thumbnail") : null)
                .size(paramatersMap.containsKey("size") ? (Integer) paramatersMap.get("size") : null)
                .build();
   }
}
