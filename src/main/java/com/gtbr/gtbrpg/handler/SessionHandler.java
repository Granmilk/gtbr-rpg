package com.gtbr.gtbrpg.handler;

import com.gtbr.gtbrpg.domain.dto.SessionRecord;
import com.gtbr.gtbrpg.domain.entity.Session;
import com.gtbr.gtbrpg.service.SessionService;
import com.gtbr.gtbrpg.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Service;

import static com.gtbr.gtbrpg.util.Constants.*;
import static com.gtbr.gtbrpg.util.MessageUtil.addDefaultReaction;

@Service
@RequiredArgsConstructor
public class SessionHandler implements CommandTypeHandler{

    private final SessionService sessionService;

    @Override
    public void handle(String command, Message message) {
        switch (command){
            case MINHA_SESSAO -> {
                handleMySession(message);
                addDefaultReaction(message);
            }
            case CRIAR_SESSAO -> {
                handleCreateSession(message);
                addDefaultReaction(message);
            }
            case ENTRAR_SESSAO -> {
                handleJoin(message);
                addDefaultReaction(message);
            }
            case SAIR_SESSAO -> {
                handleLeave(message);
                addDefaultReaction(message);
            }
            case EDITAR_SESSAO -> {
                handleUpdate(message);
                addDefaultReaction(message);
            }
            case INICIAR_SESSAO -> {
                handleStart(message);
                addDefaultReaction(message);
            }
            case FINALIZAR_SESSAO -> {
                handleFinish(message);
                addDefaultReaction(message);
            }
            case CONSULTAR_SESSAO -> {
                handleFind(message);
                addDefaultReaction(message);
            }
        }
    }

    private void handleFind(Message message) {

    }

    private void handleFinish(Message message) {

    }

    private void handleStart(Message message) {

    }

    private void handleUpdate(Message message) {

    }

    private void handleLeave(Message message) {
        Session session = sessionService.leaveSession(MessageUtil.getDeafaultIdNumberFromMessage(message), message.getAuthor().getId());
        EmbedBuilder embedBuilder = MessageUtil.buildEmbedSessionMessage(session);
        message.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    private void handleJoin(Message message) {
        Session session = sessionService.joinSession(MessageUtil.getDeafaultIdNumberFromMessage(message), message.getAuthor().getId());
        EmbedBuilder embedBuilder = MessageUtil.buildEmbedSessionMessage(session);
        message.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    private void handleMySession(Message message) {
        SessionRecord sessionRecord = sessionService.findMySession(message.getAuthor().getId());
        if (!sessionRecord.registeredSessions().isEmpty()) {
            message.getChannel().sendMessage("Você está registrado nessas sessões").queue();
            sessionRecord.registeredSessions().forEach(session -> {
                message.getChannel().sendMessage(MessageUtil.buildEmbedSessionMessage(session).build()).queue();
            });
        }
        if (!sessionRecord.canRegister().isEmpty()){
            message.getChannel().sendMessage("Você pode se registrar nas seguintes sessões").queue();
            sessionRecord.canRegister().forEach(session -> {
                message.getChannel().sendMessage(MessageUtil.buildEmbedSessionMessage(session).build()).queue();
            });
        }

        if (sessionRecord.registeredSessions().isEmpty() && sessionRecord.canRegister().isEmpty()) {
            message.getChannel().sendMessage(new EmbedBuilder().setTitle("Sem sessões disponiveis").build()).queue();
        }
    }

    private void handleCreateSession(Message message) {
        Session session = sessionService.createSession(MessageUtil.getParamatersMap(message, MessageUtil.getCommandOfMessage(message)), message.getAuthor().getId());
        EmbedBuilder embedBuilder = MessageUtil.buildEmbedSessionMessage(session);
        message.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
