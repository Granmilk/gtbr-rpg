package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.Request;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends CrudRepository<Request, Integer> {

    @Query("select r from Request r where r.processed = FALSE or r.requestStatus = r.processIfStatus")
    List<Request> findAllToProcess();

}
