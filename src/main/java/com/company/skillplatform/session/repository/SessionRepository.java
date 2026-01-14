package com.company.skillplatform.session.repository;

import com.company.skillplatform.session.dto.HostedSessionCardResponse;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.enums.SessionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByPostId(UUID postId);

    void deleteByPostId(UUID postId);
    List<Session> findByPostIdIn(List<UUID> postIds);

    @Query("""
        select (count(s) > 0)
        from Session s
        where s.hostUserId = :hostUserId
          and s.status = SCHEDULED
          and s.id <> :excludeSessionId
          and s.startsAt < :endAt
          and s.endsAt   > :startsAt
    """)
    boolean existsHostedOverlap(
            @Param("hostUserId") UUID hostUserId,
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeSessionId") UUID excludeSessionId
    );
    @Query("""
        select s
        from Session s
        where s.status = :status
          and s.endsAt <= :now
        order by s.endsAt asc
    """)
    List<Session> findEndedLive(
            @Param("status") SessionStatus status,
            @Param("now") LocalDateTime now
    );
    @Query("""
  select s
  from Session s
  where s.status = :status
    and s.startsAt <= :nowPlus10
    and s.endsAt   >  :now
""")
    List<Session> findToGoLive(
            @Param("status") SessionStatus status,
            @Param("now") LocalDateTime now,
            @Param("nowPlus10") LocalDateTime nowPlus10
    );
    @Query("""
    select count(s)
    from Session s
    where s.hostUserId = :hostUserId
      and s.status in :activeStatuses
      and (:excludeSessionId is null or s.id <> :excludeSessionId)
      and s.startsAt < :newEndAt
      and s.endsAt > :newStartsAt
""")
    long countHostOverlaps(
            @Param("hostUserId") UUID hostUserId,
            @Param("newStartsAt") LocalDateTime newStartsAt,
            @Param("newEndAt") LocalDateTime newEndAt,
            @Param("excludeSessionId") UUID excludeSessionId,
            @Param("activeStatuses") Set<SessionStatus> activeStatuses
    );

    @Query("""
    select
      s.id as id,
      s.title as title,
      s.startsAt as startsAt,
      s.endsAt as endAt
    from Session s
    join User u on u.id = s.hostUserId
    where u.department = :department
      and s.status in :activeStatuses
      and (:excludeSessionId is null or s.id <> :excludeSessionId)
      and s.startsAt < :newEndAt
      and s.endsAt > :newStartsAt
    order by s.startsAt asc
""")
    List<SessionConflictRow> findDepartmentOverlaps(
            @Param("department") String department,
            @Param("newStartsAt") LocalDateTime newStartsAt,
            @Param("newEndAt") LocalDateTime newEndAt,
            @Param("excludeSessionId") UUID excludeSessionId,
            @Param("activeStatuses") Set<SessionStatus> activeStatuses,
            Pageable pageable
    );

    @Query("""
    select s
    from Session s
    where s.status in :activeStatuses
      and s.startsAt >= :fromTime
      and s.startsAt < :toTime
      and s.hostReminderSentAt is null
""")
    List<Session> findHostReminderDue(
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("activeStatuses") Set<SessionStatus> activeStatuses
    );
    @Query("""
    select s
    from Session s
    where s.status in :activeStatuses
      and s.startsAt >= :fromTime
      and s.startsAt < :toTime
      and s.registrantsReminderSentAt is null
""")
    List<Session> findRegistrantsReminderDue(
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("activeStatuses") Set<SessionStatus> activeStatuses
    );

    @Query("""
    select new com.company.skillplatform.session.dto.HostedSessionCardResponse(
        s.id,
        s.postId,
        s.title,
        s.startsAt,
        s.endsAt,
        s.durationMinutes,
        s.meetingLink,
        s.recordingUrl,
        s.status,
       coalesce(count(distinct a.userId), 0L)
    )
    from Session s
    join Post p on p.id = s.postId
    left join SessionAttendance a on a.sessionId = s.id
    where s.hostUserId = :hostUserId
      and p.status = com.company.skillplatform.post.enums.PostStatus.PUBLISHED
      and s.status in :statuses
      and (:cursorStartsAt is null or (s.startsAt < :cursorStartsAt
           or (s.startsAt = :cursorStartsAt and s.id < :cursorId)))
    group by s.id, s.postId, s.title, s.startsAt, s.endsAt, s.durationMinutes, s.meetingLink, s.recordingUrl, s.status
    order by s.startsAt desc, s.id desc
""")
    List<HostedSessionCardResponse> findHostedSessionsPage(
            @Param("hostUserId") UUID hostUserId,
            @Param("statuses") Set<SessionStatus> statuses,
            @Param("cursorStartsAt") LocalDateTime cursorStartsAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    @Query("""
      select count(s.id)
      from Session s
      where s.hostUserId = :hostId
        and s.status = :status
    """)
    long countByHostAndStatus(@Param("hostId") UUID hostId,
                              @Param("status") SessionStatus status);

}
