package com.gtbr.gtbrpg.util;

import com.gtbr.gtbrpg.domain.entity.Player;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static void hasPermission(Player userRequest, List<Player> usersAuthorized, boolean masterByPass){
        if (!(masterByPass && userRequest.isAdmin()) && !in(userRequest.getDiscordId(), usersAuthorized.stream().map(Player::getDiscordId).collect(Collectors.toList())))
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
}
