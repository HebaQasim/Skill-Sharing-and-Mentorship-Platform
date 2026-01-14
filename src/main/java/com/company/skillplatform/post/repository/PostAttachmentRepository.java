package com.company.skillplatform.post.repository;

import com.company.skillplatform.post.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, UUID> {


    List<PostAttachment> findByPostIdOrderByCreatedAtAsc(UUID postId);

    Optional<PostAttachment> findByIdAndPostId(UUID id, UUID postId);

    List<PostAttachment> findByPostIdInOrderByPostIdAscCreatedAtAsc(List<UUID> postIds);

    long countByPostId(UUID postId);

}

