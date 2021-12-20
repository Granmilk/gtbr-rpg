package com.gtbr.gtbrpg.handler;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

import org.springframework.stereotype.Service;

import static com.gtbr.gtbrpg.util.Constants.*;
import static com.gtbr.gtbrpg.util.MessageUtil.replaceEmote;

import com.gtbr.gtbrpg.util.MessageUtil;

@Service
@RequiredArgsConstructor
public class SessionHandler implements CommandTypeHandler{

    @Override
    public void handle(String command, Message message) {
        switch (command){
            case MINHA_SESSAO -> {}
            case CRIAR_SESSAO -> {
                handleCreateSession(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
        }
    }

    private void handleCreateSession(Message message) {

    }
}
