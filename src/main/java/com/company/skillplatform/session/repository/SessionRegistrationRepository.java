package com.company.skillplatform.session.repository;

import com.company.skillplatform.session.entity.SessionRegistration;
import com.company.skillplatform.session.enums.SessionStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SessionRegistrationRepository extends JpaRepository<SessionRegistration, UUID> {

    boolean existsBySessionIdAndUserId(UUID sessionId, UUID userId);

    void deleteBySessionIdAndUserId(UUID sessionId, UUID userId);

    long countBySessionId(UUID sessionId);

    /**
     * Conflict #1: user already registered to another scheduled session overlapping the given time.
     */
    @Query("""
        select (count(r) > 0)
        from SessionRegistration r
        join Session s on s.id = r.sessionId
        where r.userId = :userId
          and s.status = SessionStatus.SCHEDULED
          and s.id <> :excludeSessionId
          and s.startsAt < :endAt
          and s.endsAt   > :startsAt
    """)
    boolean existsRegisteredOverlap(
            @Param("userId") UUID userId,
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeSessionId") UUID excludeSessionId
    );
    @Query("""
        select r.sessionId as sessionId, count(r.id) as cnt
        from SessionRegistration r
        where r.sessionId in :sessionIds
        group by r.sessionId
    """)
    List<SessionRegCountRow> countBySessionIds(@Param("sessionIds") Collection<UUID> sessionIds);

    interface SessionRegCountRow {
        UUID getSessionId();
        long getCnt();
    }

    @Query("""
      select
        u.id as userId,
        concat(u.firstName, ' ', u.lastName) as fullName,
        u.department as department,
        u.jobTitle as jobTitle,
        r.createdAt as joinedAt
      from SessionRegistration r
      join User u on u.id = r.userId
      where r.sessionId = :sessionId
      order by r.createdAt asc
    """)
    List<RegisteredRow> listRegistered(@Param("sessionId") UUID sessionId);

    interface RegisteredRow {
        UUID getUserId();
        String getFullName();
        String getDepartment();
        String getJobTitle();
        LocalDateTime getJoinedAt();
    }
    @Query("""
      select r.userId
      from SessionRegistration r
      where r.sessionId = :sessionId
    """)
    List<UUID> findRegistrantUserIds(@Param("sessionId") UUID sessionId);

    @Query("""
    select count(r)
    from SessionRegistration r
    join Session s on s.id = r.sessionId
    where r.userId = :userId
      and s.status in :activeStatuses
      and s.startsAt < :newEndAt
      and s.endsAt > :newStartsAt
""")
    long countUserRegisteredOverlaps(
            @Param("userId") UUID userId,
            @Param("newStartsAt") LocalDateTime newStartsAt,
            @Param("newEndAt") LocalDateTime newEndAt,
            @Param("activeStatuses") Set<SessionStatus> activeStatuses
    );

    @Query("select r.userId from SessionRegistration r where r.sessionId = :sessionId")
    List<UUID> findUserIdsBySessionId(@Param("sessionId") UUID sessionId);
}
