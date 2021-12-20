package com.gtbr.gtbrpg.domain.enums;

import lombok.Getter;

import static com.gtbr.gtbrpg.util.Constants.*;

import java.util.Arrays;

@Getter
public enum CommandType {
    GROUP(MEU_GRUPO,
            VER_GRUPO,
            CRIAR_GRUPO,
            EDITAR_GRUPO,
            FECHAR_GRUPO,
            ENTRAR_GRUPO,
            SAIR_GRUPO,
            CONVIDAR_GRUPO),

    SESSION(MINHA_SESSAO,
            CONSULTAR_SESSAO,
            CRIAR_SESSAO,
            EDITAR_SESSAO,
            FECHAR_SESSAO,
            ENTRAR_SESSAO,
            SAIR_SESSAO),

    REQUEST(ACEITAR_REQUISICAO,
            REJEITAR_REQUISICAO);

    private String[] commands;

    CommandType(String... commands){
        this.commands = commands;
    }

    public static CommandType of(String type) {
        for (CommandType commandType : values()){
            if (Arrays.stream(commandType.getCommands()).anyMatch(command -> command.equalsIgnoreCase(type)))
                return commandType;
        }

        throw new RuntimeException("Tipo de comando não encontrado!");
    }
}
