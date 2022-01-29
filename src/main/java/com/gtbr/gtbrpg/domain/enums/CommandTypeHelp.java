package com.gtbr.gtbrpg.domain.enums;

import lombok.Getter;

import static com.gtbr.gtbrpg.domain.enums.CommandDetails.*;

@Getter
public enum CommandTypeHelp {
    GROUP(MEU_GRUPO, VER_GRUPO, CRIAR_GRUPO, EDITAR_GRUPO, FECHAR_GRUPO, ENTRAR_GRUPO, SAIR_GRUPO, CONVIDAR_GRUPO),
    SESSION(MINHA_SESSAO, CONSULTAR_SESSAO, CRIAR_SESSAO, EDITAR_SESSAO, FECHAR_SESSAO, ENTRAR_SESSAO, SAIR_SESSAO, INICIAR_SESSAO, FINALIZAR_SESSAO);

    private final CommandDetails[] commandDetails;

    CommandTypeHelp(CommandDetails... commandDetails) {
        this.commandDetails = commandDetails;
    }
}
