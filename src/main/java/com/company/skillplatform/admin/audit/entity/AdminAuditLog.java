package com.company.skillplatform.admin.audit.entity;

import com.company.skillplatform.admin.audit.enums.AdminAuditAction;
import com.company.skillplatform.admin.audit.enums.AdminAuditTargetType;
import com.company.skillplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "admin_audit_logs",
        indexes = {
                @Index(name = "idx_audit_moderator", columnList = "moderatorUserId,createdAt"),
                @Index(name = "idx_audit_target", columnList = "targetType,targetId,createdAt"),
                @Index(name = "idx_audit_action", columnList = "action,createdAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLog extends BaseEntity {

    @Column(nullable = false)
    private UUID moderatorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdminAuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdminAuditTargetType targetType;

    @Column(nullable = false)
    private UUID targetId;


    @Column
    private UUID contextId;

    @Column(length = 500)
    private String note;

    // optional: request metadata
    @Column(length = 60)
    private String ip;

    @Column(length = 300)
    private String userAgent;
}

