package com.gtbr.gtbrpg.util;

import java.util.Map;

public class ParameterUtils {
    public static void validadeCreateGroupParameters(Map<String, Object> parameters) {
        if (!parameters.containsKey("nome")) throw new RuntimeException("Parametro `nome` e obrigatorio");
        if (!parameters.containsKey("descricao")) throw new RuntimeException("Parametro `descricao` e obrigatorio");
    }
}
