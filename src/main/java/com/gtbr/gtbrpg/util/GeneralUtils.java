package com.gtbr.gtbrpg.util;

import java.util.List;

public class GeneralUtils {

    public static boolean in(String discordId, List<String> idList) {
        return idList.stream().anyMatch(id -> id.equals(discordId));
    }
}
