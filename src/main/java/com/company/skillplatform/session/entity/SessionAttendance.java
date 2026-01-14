package com.company.skillplatform.session.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "session_attendance",
        uniqueConstraints = @UniqueConstraint(name = "uk_attendance_session_user", columnNames = {"sessionId", "userId"}),
        indexes = {
                @Index(name = "idx_attendance_session", columnList = "sessionId"),
                @Index(name = "idx_attendance_user", columnList = "userId"),
                @Index(name = "idx_attendance_session_first", columnList = "sessionId,firstJoinedAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionAttendance extends BaseEntity {

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private int joinCount;

    @Column(nullable = false)
    private LocalDateTime firstJoinedAt;

    @Column(nullable = false)
    private LocalDateTime lastJoinedAt;

    public void markJoinNow() {
        LocalDateTime now = LocalDateTime.now();
        this.joinCount++;
        this.lastJoinedAt = now;
        if (this.firstJoinedAt == null) this.firstJoinedAt = now;
    }

    public static SessionAttendance newFirstJoin(UUID sessionId, UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        return SessionAttendance.builder()
                .sessionId(sessionId)
                .userId(userId)
                .joinCount(1)
                .firstJoinedAt(now)
                .lastJoinedAt(now)
                .build();
    }
}

