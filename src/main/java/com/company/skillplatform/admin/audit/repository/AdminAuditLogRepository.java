package com.company.skillplatform.admin.audit.repository;

import com.company.skillplatform.admin.audit.entity.AdminAuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {


    // first page
    List<AdminAuditLog> findByOrderByCreatedAtDescIdDesc(Pageable pageable);

    // next page (cursor)
    @Query("""
        select a
        from AdminAuditLog a
        where (a.createdAt < :createdAt)
           or (a.createdAt = :createdAt and a.id < :id)
        order by a.createdAt desc, a.id desc
    """)
    List<AdminAuditLog> findNextPage(
            @Param("createdAt") LocalDateTime createdAt,
            @Param("id") UUID id,
            Pageable pageable
    );
}

