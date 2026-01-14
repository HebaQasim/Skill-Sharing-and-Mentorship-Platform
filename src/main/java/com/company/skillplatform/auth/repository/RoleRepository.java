package com.company.skillplatform.auth.repository;

import com.company.skillplatform.auth.entity.Role;

import com.company.skillplatform.auth.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);
}

