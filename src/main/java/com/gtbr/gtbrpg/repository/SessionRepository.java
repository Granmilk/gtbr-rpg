package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.Session;
import com.gtbr.gtbrpg.domain.enums.SessionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends CrudRepository<Session, Integer> {

    @Query("select s from Session s " +
            "where s.canList = true and s.finished is null " +
            "and s.started is null and s.sessionStatus.id = 1 " +
            "and s.group is null and s.player is null " +
            "and s.sessionType = :type")
    Session findAvailableSessionsBySessionType(SessionType type);

    @Query("select s from Session s " +
            "where s.canList = true and s.finished is null " +
            "and s.sessionStatus.id = 1 and s.sessionType = 'SOLO' " +
            "and s.player.playerId = :playerId")
    Optional<Session> findSessionByPlayerId(Integer playerId);

    @Query("select s from Session s " +
            "where s.canList = true and s.finished is null " +
            "and s.sessionStatus.id = 1 and s.sessionType = 'GROUP'" +
            "and s.group.groupId = :groupId")
    Optional<Session> findSessionByGroupId(Integer groupId);

    @Query("select s from Session s " +
            "where s.canList = true and s.finished is null " +
            "and s.sessionStatus.id = 1 and s.started is null " +
            "and s.sessionId = :sessionId")
    Optional<Session> findAvailableSessionById(Integer sessionId);

    @Query("select s from Session s " +
            "where s.started is null and s.finished is null " +
            "and s.sessionStatus.id = 1 and s.scheduledTo between :today and :targetDate")
    List<Session> findAvailableSessionsForNextDays(LocalDateTime today, LocalDateTime targetDate);
}
