package com.company.skillplatform.auth.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens",
        indexes = {@Index(name = "idx_user_device", columnList = "user_id, deviceId")})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity {



    @Column(nullable = false, unique = true)
    private String hashedToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, unique = true, length = 36)
    private String jti;

    @Version
    private long version;
    @Column(nullable = false)

    private String deviceId;
    @Column(nullable = false)
    private String deviceInfo;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private boolean revoked;
}
