package com.company.skillplatform.mentor.repository;

import com.company.skillplatform.mentor.entity.MentorBadgeRequest;
import com.company.skillplatform.mentor.enums.MentorRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface MentorBadgeRequestRepository extends JpaRepository<MentorBadgeRequest, UUID> {

    boolean existsByUserIdAndStatus(UUID userId, MentorRequestStatus status);

    Optional<MentorBadgeRequest> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    List<MentorBadgeRequest> findByStatusOrderByCreatedAtAsc(MentorRequestStatus status);
}
