package com.gtbr.gtbrpg.service;

import com.gtbr.gtbrpg.domain.entity.Player;
import com.gtbr.gtbrpg.domain.entity.Status;
import com.gtbr.gtbrpg.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player getPlayerByDiscordId(String discordId) {
        return playerRepository.findByDiscordId(discordId).orElseThrow(() -> {
            throw new RuntimeException("Player nao encontrado.");
        });
    }

    public void savePlayerByMember(Member member) {
        if (playerRepository.findByDiscordId(member.getId()).isEmpty()) {
            log.info("Registrando player {}", member.getUser().getAsTag());
            playerRepository.save(Player.builder()
                    .discordId(member.getId())
                    .name(member.getEffectiveName())
                    .tag(member.getUser().getAsTag())
                    .guildId(member.getGuild().getId())
                    .status(Status.builder().id(1).build())
                    .build());
        }
    }
}
