package com.gtbr.gtbrpg.handler;

import com.gtbr.gtbrpg.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
public class HelpHandler implements CommandTypeHandler {
    @Override
    public void handle(String command, Message message) {
        MessageUtil.buildEmbedHelpMessage().forEach(messageEmbed -> message.getChannel().sendMessageEmbeds(messageEmbed).queue());
    }
}
