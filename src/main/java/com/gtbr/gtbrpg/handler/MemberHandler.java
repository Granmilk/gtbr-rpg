package com.gtbr.gtbrpg.handler;

import com.gtbr.gtbrpg.domain.enums.HandleType;
import com.gtbr.gtbrpg.service.PlayerService;
import com.gtbr.gtbrpg.util.SpringContext;
import net.dv8tion.jda.api.entities.Member;

public class MemberHandler {

    private final PlayerService playerService;

    public MemberHandler(){
        this.playerService = SpringContext.getBean(PlayerService.class);
    }

    public void handle(Member member, HandleType handleType) {

        switch (handleType){
            case REGISTER_ON_READY -> playerService.savePlayerByMember(member);
        }

    }
}
