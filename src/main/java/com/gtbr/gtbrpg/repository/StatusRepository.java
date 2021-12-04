package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.Status;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends CrudRepository<Status, Integer> {

}
