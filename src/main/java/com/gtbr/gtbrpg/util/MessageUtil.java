package com.gtbr.gtbrpg.util;

import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    public static void hasPermission(){

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
}
