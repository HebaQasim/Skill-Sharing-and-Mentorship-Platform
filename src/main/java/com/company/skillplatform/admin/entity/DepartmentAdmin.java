package com.company.skillplatform.admin.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "department_admins",
        uniqueConstraints = @UniqueConstraint(name = "uk_department_admin", columnNames = {"department"}),
        indexes = {
                @Index(name = "idx_department_admin_department", columnList = "department"),
                @Index(name = "idx_department_admin_user", columnList = "admin_user_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentAdmin extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User admin;
}
