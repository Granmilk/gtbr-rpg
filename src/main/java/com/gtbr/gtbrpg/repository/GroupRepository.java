package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends CrudRepository<Group, Integer> {

}
