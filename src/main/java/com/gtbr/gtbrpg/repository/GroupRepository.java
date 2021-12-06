package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.Group;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends CrudRepository<Group, Integer> {

    @Query("select g from Group g where g.name like :nome")
    Optional<Group> findByName(String nome);
}
