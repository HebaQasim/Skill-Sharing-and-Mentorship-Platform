package com.company.skillplatform.post.service.impl;

import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.common.storage.StorageResult;
import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.post.dto.PostAttachmentResponse;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.entity.PostAttachment;
import com.company.skillplatform.post.enums.AttachmentType;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.repository.PostAttachmentRepository;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.post.service.PostAttachmentManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostAttachmentManageServiceImpl implements PostAttachmentManageService {

    private final PostRepository postRepository;
    private final PostAttachmentRepository attachmentRepository;
    private final StorageService storageService;

    @Override
    public PostAttachmentResponse add(UUID userId, UUID postId, MultipartFile file) {
        Post post = mustBeOwnerNotDeleted(userId, postId);
        validateFile(file);

        AttachmentType type = detectType(file.getContentType());
        StorageResult stored = storageService.store("posts/" + postId + "/attachments", file);

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file").trim();
        String contentType = Optional.ofNullable(file.getContentType()).orElse("").trim();

        PostAttachment saved = attachmentRepository.save(PostAttachment.builder()
                .postId(postId)
                .uploadedByUserId(userId)
                .type(type)
                .originalFilename(originalName)
                .contentType(contentType)
                .sizeBytes(file.getSize())
                .storageKey(stored.storageKey())
                .build());

        return new PostAttachmentResponse(
                saved.getId(),
                saved.getType().name(),
                saved.getOriginalFilename(),
                saved.getContentType(),
                saved.getSizeBytes(),
                storageService.signedUrl(saved.getStorageKey()),
                saved.getCreatedAt()
        );
    }

    @Override
    public void delete(UUID userId, UUID postId, UUID attachmentId) {
        Post post = mustBeOwnerNotDeleted(userId, postId);

        PostAttachment att = attachmentRepository.findByIdAndPostId(attachmentId, postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ATTACHMENT_NOT_FOUND, "Attachment not found"));

        storageService.delete(att.getStorageKey());
        attachmentRepository.delete(att);
    }

    private Post mustBeOwnerNotDeleted(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (post.getStatus() == PostStatus.DELETED) {
            throw new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found");
        }
        if (!post.isOwner(userId)) {
            throw new ForbiddenException("Not allowed");
        }
        return post;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File is required");
        }
        /*
        long maxBytes = 20L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File too large (max 20MB)");
        }

         */
    }

    private AttachmentType detectType(String contentType) {
        if (contentType == null) return AttachmentType.FILE;
        if (contentType.startsWith("image/")) return AttachmentType.IMAGE;
        if (contentType.startsWith("video/")) return AttachmentType.VIDEO;
        return AttachmentType.FILE;
    }
}
