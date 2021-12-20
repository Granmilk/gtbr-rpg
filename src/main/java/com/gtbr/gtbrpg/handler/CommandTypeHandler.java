package com.gtbr.gtbrpg.handler;

import net.dv8tion.jda.api.entities.Message;

public interface CommandTypeHandler {

    void handle(String command, Message message);
}
