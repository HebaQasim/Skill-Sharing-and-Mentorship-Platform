package com.company.skillplatform.session.repository;

import com.company.skillplatform.session.entity.SessionAttendance;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, UUID> {

    Optional<SessionAttendance> findBySessionIdAndUserId(UUID sessionId, UUID userId);

    @Query("""
      select a.sessionId as sessionId, count(a.id) as cnt
      from SessionAttendance a
      where a.sessionId in :sessionIds
      group by a.sessionId
    """)
    List<AttendCountRow> countAttendedBySessionIds(@Param("sessionIds") Collection<UUID> sessionIds);

    interface AttendCountRow {
        UUID getSessionId();
        long getCnt();
    }


    @Query("""
      select
        u.id as userId,
        concat(u.firstName, ' ', u.lastName) as fullName,
        u.department as department,
        u.jobTitle as jobTitle,
        a.firstJoinedAt as joinedAt
      from SessionAttendance a
      join User u on u.id = a.userId
      where a.sessionId = :sessionId
      order by a.firstJoinedAt asc
    """)
    List<AttendedRow> listAttended(@Param("sessionId") UUID sessionId);

    interface AttendedRow {
        UUID getUserId();
        String getFullName();
        String getDepartment();
        String getJobTitle();
        LocalDateTime getJoinedAt();
    }

    boolean existsBySessionIdAndUserId(UUID sessionId, UUID userId);

    interface SessionAttendeeRow {
        UUID getSessionId();
        UUID getUserId();
    }

    @Query("""
        select a.sessionId as sessionId, a.userId as userId
        from SessionAttendance a
        where a.sessionId in :sessionIds
    """)
    List<SessionAttendeeRow> attendeesBySessionIds(@Param("sessionIds") List<UUID> sessionIds);

    @Query("""
        select a.sessionId as sessionId, count(a.id) as cnt
        from SessionAttendance a
        where a.sessionId in :sessionIds
        group by a.sessionId
    """)
    List<SessionAttendanceCountRow> countAttendanceBySessionIds(
            @Param("sessionIds") Collection<UUID> sessionIds
    );

    interface SessionAttendanceCountRow {
        UUID getSessionId();
        long getCnt();
    }

    @Query("select a.userId from SessionAttendance a where a.sessionId = :sessionId")
    List<UUID> findUserIdsBySessionId(@Param("sessionId") UUID sessionId);
}
