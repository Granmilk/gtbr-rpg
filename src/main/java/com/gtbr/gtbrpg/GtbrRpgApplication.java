package com.gtbr.gtbrpg;

import com.gtbr.gtbrpg.listener.MessageListener;
import com.gtbr.gtbrpg.repository.DiscordTokenRepository;
import com.gtbr.gtbrpg.util.Constants;
import com.gtbr.gtbrpg.util.SpringContext;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.security.auth.login.LoginException;

@SpringBootApplication
@EnableAsync
public class GtbrRpgApplication {

    public static void main(String[] args) throws LoginException {
        SpringApplication.run(GtbrRpgApplication.class, args);
        DiscordTokenRepository discordTokenRepository = SpringContext.getBean(DiscordTokenRepository.class);
        JDABuilder.createDefault(discordTokenRepository.findById(Constants.TOKEN_ID).get().getDiscordToken())
                .addEventListeners(getMessageListener())
                .build();


    }

    @Bean
    public static MessageListener getMessageListener(){
        return new MessageListener();
    }

}
