package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.GroupPlayer;
import com.gtbr.gtbrpg.domain.entity.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPlayerRepository extends CrudRepository<GroupPlayer, Integer> {

    @Query("select gp from GroupPlayer gp where gp.group.groupId = :groupId")
    List<GroupPlayer> findByGroupId(Integer groupId);

    @Query("select gp from GroupPlayer gp where gp.player.playerId = :playerId")
    List<GroupPlayer> findAllGroupsByPlayerId(Integer playerId);
}
