package com.company.skillplatform.post.repository;

import com.company.skillplatform.post.dto.PostLikerResponse;
import com.company.skillplatform.post.entity.PostLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    void deleteByPostIdAndUserId(UUID postId, UUID userId);




    long countByPostId(UUID postId);
    @Query("""
    select
      pl.postId as postId,
      count(pl.id) as cnt
    from PostLike pl
    where pl.postId in :postIds
    group by pl.postId
""")
    List<LikeCountRow> countByPostIds(@Param("postIds") Collection<UUID> postIds);


    @Query("""
        select pl.postId as postId
        from PostLike pl
        where pl.userId = :userId
          and pl.postId in :postIds
    """)
    List<LikedPostIdRow> findLikedPostIds(
            @Param("userId") UUID userId,
            @Param("postIds") Collection<UUID> postIds
    );


    @Query("""
        select new com.company.skillplatform.post.dto.PostLikerResponse(
            u.id,
            concat(u.firstName, ' ', u.lastName),
            u.department,
            u.jobTitle,
            u.profileImageUrl,
            pl.createdAt
        )
        from PostLike pl
        join User u on u.id = pl.userId
        where pl.postId = :postId
        order by pl.createdAt desc, pl.id desc
    """)
    List<PostLikerResponse> findLikersFirstPage(
            @Param("postId") UUID postId,
            Pageable pageable
    );

    @Query("""
        select new com.company.skillplatform.post.dto.PostLikerResponse(
            u.id,
             concat(u.firstName, ' ', u.lastName),
            u.department,
            u.jobTitle,
            u.profileImageUrl,
            pl.createdAt
        )
        from PostLike pl
        join User u on u.id = pl.userId
        where pl.postId = :postId
          and (
               pl.createdAt < :likedAt
               or (pl.createdAt = :likedAt and pl.id < :likeId)
          )
        order by pl.createdAt desc, pl.id desc
    """)
    List<PostLikerResponse> findLikersNextPage(
            @Param("postId") UUID postId,
            @Param("likedAt") LocalDateTime likedAt,
            @Param("likeId") UUID likeId,
            Pageable pageable
    );


    @Query("""
        select pl.id
        from PostLike pl
        where pl.postId = :postId and pl.userId = :userId
    """)
    UUID findLikeId(@Param("postId") UUID postId, @Param("userId") UUID userId);

    @Query("""
        select distinct pl.userId
        from PostLike pl
        where pl.postId = :postId
    """)
    java.util.List<UUID> findDistinctLikerUserIds(@Param("postId") UUID postId);
}
