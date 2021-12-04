package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.GroupPlayer;
import com.gtbr.gtbrpg.domain.entity.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPlayerRepository extends CrudRepository<GroupPlayer, Long> {

}
