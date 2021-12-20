package com.gtbr.gtbrpg.util;

import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.entity.Player;
import com.gtbr.gtbrpg.domain.enums.CommandType;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gtbr.gtbrpg.util.Constants.CROWN_EMOJI_CODE;
import static com.gtbr.gtbrpg.util.GeneralUtils.in;

public class MessageUtil {

    public static boolean hasPrefix(String contentRaw) {
        return contentRaw.startsWith("*");
    }

    public static String getCommandOfMessage(Message message) {
        return message.getContentRaw()
                .trim()
                .replace("*", "")
                .split(" ")[0];
    }

    public static String removePrefixAndCommand(Message message) {
        return message.getContentRaw()
                .trim()
                .replace("*"+getCommandOfMessage(message), "");
    }

    public static void hasPermission(Player userRequest, List<Player> authorizedUsers, boolean masterByPass){
        if (!(masterByPass && userRequest.isAdmin()) && !in(userRequest.getDiscordId(), authorizedUsers.stream().map(Player::getDiscordId).collect(Collectors.toList())))
            throw new RuntimeException("Voce nao tem autorizacao para convidar pessoas para este grupo");
    }

    public static Map<String, Object> getParamatersMap(Message message, String command) {
        String[] parameters = message.getContentRaw().trim().replace("*" + command, "").split(",");
        Map<String, Object> mapParameter = new HashMap<>();

        for (String parameter : parameters) {
            if (parameter.startsWith("lider")) parameter = "lider="+message.getMentionedUsers().get(0).getId();
            mapParameter.put(parameter.split("=")[0].trim().toLowerCase(Locale.ROOT), parameter.split("=")[1].trim());
        }

        return mapParameter;
    }

    public static boolean hasRequestObservation(String command) {
        return command.contains(" ");
    }


    public static EmbedBuilder buildEmbedGroupMessage(GroupPlayerDto groupPlayerDto, Message message) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(groupPlayerDto.group().getName())
                .setDescription(groupPlayerDto.group().getDescription())
                .setColor(Color.MAGENTA)
                .setThumbnail(groupPlayerDto.group().getThumbnail())
                .setFooter("Preenchimento: " + String.format("(%s/%s)", groupPlayerDto.playerList().size(), groupPlayerDto.group().getSize()))
                .addBlankField(false);

        groupPlayerDto.playerList().forEach(player -> {
            embedBuilder.addField("Membro: ", message.getJDA().getUserById(player.getDiscordId()).getAsMention() +
                            (Objects.equals(groupPlayerDto.group().getLeader().getDiscordId(), player.getDiscordId()) ? " " + CROWN_EMOJI_CODE : ""),
                    true);
        });

        return embedBuilder;
    }

    public static void replaceEmote(Message message, String oldEmoji, String newEmoji) {
        message.removeReaction(oldEmoji).queue();
        message.addReaction(newEmoji).queue();
    }
}
