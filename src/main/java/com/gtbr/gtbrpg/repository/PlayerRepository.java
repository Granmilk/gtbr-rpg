package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Integer> {

    Optional<Player> findByDiscordId(String discordId);
}
