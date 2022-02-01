package com.gtbr.gtbrpg.domain.enums;

import lombok.Getter;

@Getter
public enum CommandDetails {

    MEU_GRUPO("Grupo","Mostra seus grupos"),
    LISTAR_GRUPOS("ListarGrupos", "Mostra todos os grupos ativos"),
    VER_GRUPO("ConsultarGrupo `#Id`","Visualiza o grupo especifico, precisa informar o ID"),
    CRIAR_GRUPO("CriarGrupo `<params>`","Cria um grupo de acordo com os parametros passados, os possiveis parametros sao: `nome=nome do seu grupo,descricao= descricao do seu grupo,thumbnail= link de uma imagem para representar seu grupo`"),
    EDITAR_GRUPO("EditarGrupo `<params>`", "**(Apenas mestre e lider do grupo)**Edita os grupos com os parametros passados, os possiveis parametros sao: `descricao= descricao do seu grupo,name=nome do seu grupo,thumbnail= link de uma imagem para representar seu grupo`"),
    FECHAR_GRUPO("FecharGrupo `#Id`", "**(Apenas mestre e lider do grupo)** Fecha o grupo especificado"),
    ENTRAR_GRUPO("EntrarGrupo `#Id`", "Requisita a entrada no grupo especifico"),
    SAIR_GRUPO("SairGrupo", "Sai do seu grupo atual"),
    CONVIDAR_GRUPO("ConvidarGrupo <@membro1 @membro2>", "Envia uma requisicao para a 1 ou mais membros "),


    MINHA_SESSAO("Sessao", "Mostra suas sessoes"),
    LISTAR_SESSOES("ListarSessoes", "Mostra todas as sessoes ativas"),
    CONSULTAR_SESSAO("ConsultarSessao `#Id`", "Consulta uma sessao especifica"),
    CRIAR_SESSAO("CriarSessao `<params>`", "**(Apenas mestre)** Cria uma sessao a partir dos parametros: ``` titulo=\"titulo\" *,\n" +
            "descricao=\"descricao\" *,\n" +
            "data=dd/MM/yyyy-HH:mm *,\n" +
            "tipo=SOLO|GROUP *,\n" +
            "thumbnail=\"URL\",\n" +
            "podeConsultar=true|false (default=true)\n" +
            "\n" +
            "*=obrigatório```"),
    EDITAR_SESSAO("EditarSessao `<params>`", "**(Apenas mestre)** Edita uma sessao a partir dos parametros: ```titulo=\"titulo\",\n" +
            "descricao=\"descricao\",\n" +
            "data=dd/MM/yyyy-HH:mm,\n" +
            "tipo=SOLO|GROUP,\n" +
            "thumbnail=\"URL\",\n" +
            "podeConsultar=true|false (default=true)```\n" +
            "\n"),
    ENTRAR_SESSAO("EntrarSessao `#Id`", "Entra na sessao especificada"),
    SAIR_SESSAO("SairSessao `#Id`", "Sai da sessao especificado caso voce esteja nela"),
    INICIAR_SESSAO("IniciarSessao", "**(Apenas mestre)** Inicia a sessao"),
    FINALIZAR_SESSAO("FinalizarSessao `#id`", "**(Apenas mestre)** Finaliza a sessao especifica");

    private final String command;
    private final String description;

    CommandDetails(String command, String description) {
        this.command = command;
        this.description = description;
    }
}