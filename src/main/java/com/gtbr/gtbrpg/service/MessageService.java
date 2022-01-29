package com.gtbr.gtbrpg.service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

public class MessageService {

    public static void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    public static void sendEmbbedMessage(MessageChannel channel, EmbedBuilder embedBuilder) {
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
