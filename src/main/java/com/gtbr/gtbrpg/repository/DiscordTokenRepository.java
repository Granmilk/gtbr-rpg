package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.DiscordToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordTokenRepository extends CrudRepository<DiscordToken, Long> {
}
