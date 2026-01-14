package com.company.skillplatform.auth.entity;

import com.company.skillplatform.auth.enums.RoleName;
import com.company.skillplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "roles",
        indexes = {
                @Index(name = "idx_roles_name", columnList = "name")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity {


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, unique = true)
    private RoleName name;
}

