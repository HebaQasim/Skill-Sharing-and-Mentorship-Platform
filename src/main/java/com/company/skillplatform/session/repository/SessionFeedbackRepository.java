package com.company.skillplatform.session.repository;

import com.company.skillplatform.session.entity.SessionFeedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionFeedbackRepository extends JpaRepository<SessionFeedback, UUID> {

    Optional<SessionFeedback> findBySessionIdAndAuthorUserId(UUID sessionId, UUID authorUserId);

    Optional<SessionFeedback> findByIdAndSessionId(UUID id, UUID sessionId);

    @Query("""
        select
          f.id as id,
          f.sessionId as sessionId,
          f.rating as rating,
          f.comment as comment,
          f.createdAt as createdAt,
          f.editedAt as editedAt,
          f.deletedAt as deletedAt,

          u.id as authorId,
          concat(u.firstName, ' ', u.lastName) as authorFullName,
          u.department as authorDepartment,
          u.jobTitle as authorJobTitle,
          u.headline as authorHeadline,
          u.profileImageUrl as authorProfileImageUrl
        from SessionFeedback f
        join User u on u.id = f.authorUserId
        where f.sessionId = :sessionId
        order by f.createdAt desc, f.id desc
    """)
    List<FeedbackRow> firstPage(@Param("sessionId") UUID sessionId, Pageable pageable);

    @Query("""
        select
          f.id as id,
          f.sessionId as sessionId,
          f.rating as rating,
          f.comment as comment,
          f.createdAt as createdAt,
          f.editedAt as editedAt,
          f.deletedAt as deletedAt,

          u.id as authorId,
          concat(u.firstName, ' ', u.lastName) as authorFullName,
          u.department as authorDepartment,
          u.jobTitle as authorJobTitle,
          u.headline as authorHeadline,
          u.profileImageUrl as authorProfileImageUrl
        from SessionFeedback f
        join User u on u.id = f.authorUserId
        where f.sessionId = :sessionId
          and (
              f.createdAt < :cursorCreatedAt
              or (f.createdAt = :cursorCreatedAt and f.id < :cursorId)
          )
        order by f.createdAt desc, f.id desc
    """)
    List<FeedbackRow> nextPage(
            @Param("sessionId") UUID sessionId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    @Query("""
    select count(sf.id)
    from SessionFeedback sf
    join Session s on s.id = sf.sessionId
    where s.postId = :postId
""")
    long countByPostId(@Param("postId") UUID postId);

}

