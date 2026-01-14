package com.company.skillplatform.user.entity;

import com.company.skillplatform.auth.entity.Role;
import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.skill.entity.Skill;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_department", columnList = "department"),
                @Index(name = "idx_users_enabled", columnList = "enabled")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {



    @Column(nullable = false, length = 100, updatable = false)
    private String firstName;

    @Column(nullable = false, length = 100, updatable = false)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150, updatable = false)
    private String email;

    /* Authentication */

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    /* Profile */

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 100)
    private String jobTitle;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(length = 500)
    private String profileImageUrl;

    /* Mentor badge (NOT a role) */

    @Column(nullable = false)
    private boolean mentor = false;

    private LocalDateTime mentorGrantedAt;

    /* Authorization */

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            indexes = {
                    @Index(name = "idx_user_roles_user", columnList = "user_id"),
                    @Index(name = "idx_user_roles_role", columnList = "role_id")
            }
    )
    private Set<Role> roles = new HashSet<>();
    @Column(length = 220)
    private String headline;
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"),
            indexes = {
                    @Index(name = "idx_user_skills_user", columnList = "user_id"),
                    @Index(name = "idx_user_skills_skill", columnList = "skill_id")
            }
    )
    private Set<Skill> skills = new HashSet<>();

    public void addSkill(Skill skill) {
        this.skills.add(skill);
    }

    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
    }

    /* Derived */

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void updateSelfFields(String newPhoneNumber, String newHeadline) {
        if (newPhoneNumber != null) {
            this.phoneNumber = newPhoneNumber;
        }
        if(newHeadline!=null) {
            this.headline = newHeadline;
        }
    }
    public void applyApprovedProfileChange(String newDepartment, String newJobTitle) {
        if (newDepartment != null) {
            this.department = newDepartment;
        }
        if (newJobTitle != null) {
            this.jobTitle = newJobTitle;
        }
    }


    public void setPassword(String encode) {
        this.password=encode;
    }

    public void setEnabled(boolean newEnabled) {
        this.enabled=newEnabled;
    }

    public boolean isMentor() { return mentor; }

    public void grantMentor() {
        this.mentor = true;
        this.mentorGrantedAt = LocalDateTime.now();
    }
}

