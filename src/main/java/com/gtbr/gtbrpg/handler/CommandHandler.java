package com.gtbr.gtbrpg.handler;


import com.gtbr.gtbrpg.domain.enums.CommandType;
import com.gtbr.gtbrpg.service.MessageService;
import com.gtbr.gtbrpg.util.MessageUtil;
import com.gtbr.gtbrpg.util.SpringContext;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;

import static com.gtbr.gtbrpg.util.Constants.*;
import static com.gtbr.gtbrpg.util.MessageUtil.replaceEmote;

@Slf4j
public class CommandHandler {

    private final GroupHandler groupHandler;
    private final SessionHandler sessionHandler;
    private final RequestHandler requestHandler;

    public CommandHandler() {
        this.groupHandler = SpringContext.getBean(GroupHandler.class);
        this.sessionHandler = SpringContext.getBean(SessionHandler.class);
        this.requestHandler = SpringContext.getBean(RequestHandler.class);
    }

    public void handle(Message message) {
        try {
            String command = MessageUtil.getCommandOfMessage(message).toUpperCase();
            log.info("[INITIALIZING COMMAND HANDLER] - [COMMAND:{}]", command);
            switch (CommandType.of(command)) {
                case GROUP -> groupHandler.handle(command, message);
                case SESSION -> sessionHandler.handle(command, message);
                case REQUEST -> requestHandler.handle(command, message);
                default -> {
                    replaceEmote(message, RELOADING_EMOJI_CODE, WHAT_EMOJI_CODE);
                    MessageService.sendMessage(message.getChannel(), "Comando nao reconhecido!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.removeReaction("\uD83D\uDD04").queue();
            message.addReaction(ERROR_EMOJI_CODE).queue();
            MessageService.sendEmbbedMessage(message.getChannel(),
                    new EmbedBuilder()
                            .setTitle("Erro")
                            .setDescription(e.getMessage())
                            .setColor(Color.RED));
        }

    }
}