package com.company.skillplatform.session.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "session_registrations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_session_registrations_session_user",
                columnNames = {"sessionId", "userId"}
        ),
        indexes = {
                @Index(name = "idx_session_regs_session", columnList = "sessionId,createdAt"),
                @Index(name = "idx_session_regs_user", columnList = "userId,createdAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionRegistration extends BaseEntity {

    @Column(nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionId", insertable = false, updatable = false)
    private Session session;

    @Column(nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
}
