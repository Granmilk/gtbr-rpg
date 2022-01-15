package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.GroupPlayer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupPlayerRepository extends CrudRepository<GroupPlayer, Integer> {

    @Query("select gp from GroupPlayer gp where gp.group.groupId = :groupId")
    List<GroupPlayer> findByGroupId(Integer groupId);

    @Query("select gp from GroupPlayer gp where gp.player.playerId = :playerId")
    List<GroupPlayer> findAllGroupsByPlayerId(Integer playerId);

    @Query("select gp from GroupPlayer gp where gp.player.playerId = :playerId and gp.joinedAt IS NOT NULL and gp.leaveAt IS NULL")
    Optional<GroupPlayer> findActualGroupByPlayerId(Integer playerId);

    @Query("select gp from GroupPlayer gp where gp.player.playerId = :playerId and gp.leaveAt IS NOT NULL AND gp.leaveAt IS NOT NULL")
    List<GroupPlayer> findAllQuitedGroupsByPlayerId(Integer playerId);
}
