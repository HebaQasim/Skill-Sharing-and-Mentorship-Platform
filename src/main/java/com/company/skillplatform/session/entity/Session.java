package com.company.skillplatform.session.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.session.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "sessions",
        uniqueConstraints = @UniqueConstraint(name = "uk_sessions_post", columnNames = "postId"),
        indexes = {
                @Index(name = "idx_sessions_post", columnList = "postId"),
                @Index(name = "idx_sessions_host", columnList = "hostUserId"),
                @Index(name = "idx_sessions_status_time", columnList = "status,startsAt,endsAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session extends BaseEntity {

    @Column(nullable = false)
    private UUID postId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", insertable = false, updatable = false)
    private Post post;

    @Column(nullable = false)
    private UUID hostUserId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(length = 400)
    private String meetingLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;
    @Column
    private LocalDateTime canceledAt;

    @Column(length = 500)
    private String recordingUrl;

    @Column(nullable = false)
    private LocalDateTime endsAt;
    @Column
    private LocalDateTime hostReminderSentAt;
    @Column
    private LocalDateTime registrantsReminderSentAt;


    public void updateDetails(String title, LocalDateTime startsAt, int durationMinutes, String meetingLink) {
        this.title = title;
        this.startsAt = startsAt;
        this.durationMinutes = durationMinutes;
        this.meetingLink = meetingLink;
        this.endsAt = startsAt.plusMinutes(durationMinutes);
    }

    public void scheduleOnPublish() {
        this.status = SessionStatus.SCHEDULED;
        if (this.endsAt == null) {
            this.endsAt = this.startsAt.plusMinutes(this.durationMinutes);
        }
    }


    public void cancel() {
        this.status = SessionStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();

    }

    public LocalDateTime endsAt() {
        return startsAt.plusMinutes(durationMinutes);
    }

    public void markCompleted() {
        this.status = SessionStatus.COMPLETED;
    }

    public void markUnattended() {
        this.status =SessionStatus.UNATTENDED;
    }
    public void markLive() {
        this.status = SessionStatus.LIVE;
    }

    public void attachRecording(String recordingUrl) {
        this.recordingUrl = recordingUrl;
    }
    public void reschedule(LocalDateTime newStartsAt) {
        this.startsAt = newStartsAt;
        this.status = SessionStatus.SCHEDULED;
    }

}
