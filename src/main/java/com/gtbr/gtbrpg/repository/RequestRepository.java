package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.Request;
import com.gtbr.gtbrpg.domain.enums.RequestType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends CrudRepository<Request, Integer> {

    @Query("select r from Request r where r.processed = FALSE or r.requestStatus = r.processIfStatus")
    List<Request> findAllToProcess();

    @Query("select r from Request r where r.processed = FALSE or r.requestStatus = r.processIfStatus and r.requestType = :type")
    List<Request> findAllByType(RequestType type);

    Optional<Request> findByReviewerObservation(String reviewerObservation);
}
