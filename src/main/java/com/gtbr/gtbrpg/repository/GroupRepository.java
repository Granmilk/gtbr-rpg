package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.entity.Group;
import com.gtbr.gtbrpg.domain.entity.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends CrudRepository<Group, Integer> {

    @Query("select g from Group g where g.name like :nome and g.closedAt is null")
    Optional<Group> findByName(String nome);

    @Query("select g from Group g where g.closedAt is null")
    List<Group> findAvailableGroups();

    @Query("select gp.player from GroupPlayer gp where gp.group.groupId = :groupId")
    List<Player> findAllPlayersByGroup(Integer groupId);
}
