package com.gtbr.gtbrpg;

import com.gtbr.gtbrpg.listener.MessageListener;
import com.gtbr.gtbrpg.repository.DiscordTokenRepository;
import com.gtbr.gtbrpg.util.Constants;
import com.gtbr.gtbrpg.util.SpringContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.security.auth.login.LoginException;

@EnableScheduling
@SpringBootApplication
@EnableAsync
public class GtbrRpgApplication {

    private static JDA jda;

    public static void main(String[] args) throws LoginException {
        SpringApplication.run(GtbrRpgApplication.class, args);
        DiscordTokenRepository discordTokenRepository = SpringContext.getBean(DiscordTokenRepository.class);
        jda = JDABuilder.createDefault(discordTokenRepository.findById(Constants.TOKEN_ID).get().getDiscordToken())
                .addEventListeners(getMessageListener())
                .build();


    }

    @Bean
    public static MessageListener getMessageListener(){
        return new MessageListener();
    }

    public static JDA getJda(){
        return jda;
    }
}
