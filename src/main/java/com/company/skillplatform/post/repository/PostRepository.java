package com.company.skillplatform.post.repository;

import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.enums.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query("""
        select p
        from Post p
        where p.status = :status
        order by p.publishedAt desc, p.id desc
    """)
    List<Post> feedFirstPage(@Param("status") PostStatus status, Pageable pageable);

    @Query("""
        select p
        from Post p
        where p.status = :status
          and (
                p.publishedAt < :publishedAt
                or (p.publishedAt = :publishedAt and p.id < :id)
          )
        order by p.publishedAt desc, p.id desc
    """)
    List<Post> feedNextPage(
            @Param("status") PostStatus status,
            @Param("publishedAt") LocalDateTime publishedAt,
            @Param("id") UUID id,
            Pageable pageable
    );
    @Query("""
        select p
        from Post p
        where p.authorUserId = :userId
          and p.status = :status
        order by p.createdAt desc, p.id desc
    """)
    List<Post> findMyDraftsFirstPage(
            @Param("userId") UUID userId,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    @Query("""
        select p
        from Post p
        where p.authorUserId = :userId
          and p.status = :status
          and (
                p.createdAt < :cursorCreatedAt
                or (p.createdAt = :cursorCreatedAt and p.id < :cursorId)
          )
        order by p.createdAt desc, p.id desc
    """)
    List<Post> findMyDraftsAfter(
            @Param("userId") UUID userId,
            @Param("status") PostStatus status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    @Query("""
    select p
    from Post p
    where p.status = :status
      and p.authorUserId = :authorId
    order by p.publishedAt desc, p.id desc
""")
    List<Post> profilePostsFirstPage(
            @Param("authorId") UUID authorId,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    @Query("""
    select p
    from Post p
    where p.status = :status
      and p.authorUserId = :authorId
      and (p.publishedAt < :publishedAt
           or (p.publishedAt = :publishedAt and p.id < :id))
    order by p.publishedAt desc, p.id desc
""")
    List<Post> profilePostsNextPage(
            @Param("authorId") UUID authorId,
            @Param("status") PostStatus status,
            @Param("publishedAt") LocalDateTime publishedAt,
            @Param("id") UUID id,
            Pageable pageable
    );

    @Query("""
        select p
        from Post p
        where p.authorUserId = :authorId
          and p.status = :status
        order by p.publishedAt desc, p.id desc
    """)
    List<Post> myDeletedFirstPage(UUID authorId, PostStatus status, Pageable pageable);
    @Query("""
        select p
        from Post p
        where p.authorUserId = :authorId
          and p.status = :status
          and (
                p.publishedAt < :cursorTime
             or (p.publishedAt = :cursorTime and p.id < :cursorId)
          )
        order by p.publishedAt desc, p.id desc
    """)
    List<Post> myDeletedNextPage(UUID authorId, PostStatus status, LocalDateTime cursorTime, UUID cursorId, Pageable pageable);

}

