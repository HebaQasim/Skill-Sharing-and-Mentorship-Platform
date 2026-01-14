package com.company.skillplatform.post.repository;

import com.company.skillplatform.post.entity.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    Optional<PostComment> findByIdAndPostId(UUID id, UUID postId);

    @Query("""
        select
          c.id as id,
          c.postId as postId,
          c.body as body,
          c.createdAt as createdAt,
          c.editedAt as editedAt,
          c.deletedAt as deletedAt,

          u.id as authorId,
          concat(u.firstName, ' ', u.lastName) as authorFullName,
          u.department as authorDepartment,
          u.jobTitle as authorJobTitle,
          u.headline as authorHeadline,
          u.profileImageUrl as authorProfileImageUrl
        from PostComment c
        join User u on u.id = c.authorUserId
        where c.postId = :postId
        order by c.createdAt desc, c.id desc
    """)
    List<CommentRow> firstPage(@Param("postId") UUID postId, Pageable pageable);

    @Query("""
        select
          c.id as id,
          c.postId as postId,
          c.body as body,
          c.createdAt as createdAt,
          c.editedAt as editedAt,
          c.deletedAt as deletedAt,

          u.id as authorId,
          concat(u.firstName, ' ', u.lastName) as authorFullName,
          u.department as authorDepartment,
          u.jobTitle as authorJobTitle,
          u.headline as authorHeadline,
          u.profileImageUrl as authorProfileImageUrl
        from PostComment c
        join User u on u.id = c.authorUserId
        where c.postId = :postId
          and (
              c.createdAt < :cursorCreatedAt
              or (c.createdAt = :cursorCreatedAt and c.id < :cursorId)
          )
        order by c.createdAt desc, c.id desc
    """)
    List<CommentRow> nextPage(
            @Param("postId") UUID postId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );


    @Query("""
        select distinct c.authorUserId
        from PostComment c
        where c.postId = :postId
          and c.deletedAt is null
    """)
    List<UUID> distinctActiveCommenterIds(@Param("postId") UUID postId);

    @Query("""
    select c.postId as postId, count(c.id) as cnt
    from PostComment c
    where c.postId in :postIds
      and c.deletedAt is null
    group by c.postId
""")
    List<CommentCountRow> countActiveByPostIds(@Param("postIds") Collection<UUID> postIds);

    @Query("""
  select count(c.id)
  from PostComment c
  where c.postId = :postId
    and c.deletedAt is null
""")
    long countActiveByPostId(@Param("postId") UUID postId);

    @Query("""
        select distinct c.authorUserId
        from PostComment c
        where c.postId = :postId
          and c.deletedAt is null
    """)
    java.util.List<UUID> findDistinctCommenterUserIds(@Param("postId") UUID postId);
}
