package com.company.skillplatform.admin.repository;

import com.company.skillplatform.admin.entity.DepartmentAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentAdminRepository extends JpaRepository<DepartmentAdmin, UUID> {
    Optional<DepartmentAdmin> findByDepartmentIgnoreCase(String department);
}

