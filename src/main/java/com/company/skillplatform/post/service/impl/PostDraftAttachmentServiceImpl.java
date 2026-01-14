package com.company.skillplatform.post.service.impl;

import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.common.storage.StorageResult;
import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.post.dto.PostAttachmentResponse;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.entity.PostAttachment;
import com.company.skillplatform.post.enums.AttachmentType;
import com.company.skillplatform.post.enums.PostStatus;

import com.company.skillplatform.post.mapper.PostAttachmentMapper;
import com.company.skillplatform.post.repository.PostAttachmentRepository;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.post.service.PostDraftAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostDraftAttachmentServiceImpl implements PostDraftAttachmentService {

    private static final int MAX_ATTACHMENTS_PER_POST = 10;
    private static final long MAX_SIZE_BYTES = 20L * 1024 * 1024; // 20MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp",
            "video/mp4",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    private final PostRepository postRepository;
    private final PostAttachmentRepository attachmentRepository;
    private final StorageService storageService;
    private final PostAttachmentMapper mapper;

    @Override
    public PostAttachmentResponse add(UUID userId, UUID postId, MultipartFile file) {
        Post post = mustBeOwnerDraft(userId, postId);

        validateFile(file);

        long count = attachmentRepository.countByPostId(postId);
        if (count >= MAX_ATTACHMENTS_PER_POST) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Max attachments reached (10)");
        }

        String contentType = normalizeContentType(file.getContentType());
        AttachmentType type = detectType(contentType);

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file").trim();
        if (originalName.isBlank()) originalName = "file";

        StorageResult stored = storageService.store("posts/" + postId + "/attachments", file);

        PostAttachment saved = attachmentRepository.save(PostAttachment.builder()
                .postId(postId)
                .uploadedByUserId(userId)
                .type(type)
                .originalFilename(trimTo(originalName, 200))
                .contentType(trimTo(contentType, 120))
                .sizeBytes(file.getSize())
                .storageKey(stored.storageKey())
                .build());

        return mapper.toResponse(saved);
    }

    @Override
    public void remove(UUID userId, UUID postId, UUID attachmentId) {
        mustBeOwnerDraft(userId, postId);

        PostAttachment att = attachmentRepository.findByIdAndPostId(attachmentId, postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ATTACHMENT_NOT_FOUND, "Attachment not found"));

        // Best-effort delete from storage first
        storageService.delete(att.getStorageKey());
        attachmentRepository.delete(att);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostAttachmentResponse> list(UUID userId, UUID postId) {
        mustBeOwnerDraft(userId, postId);

        return attachmentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Post mustBeOwnerDraft(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) {
            throw new ForbiddenException("Not allowed");
        }

        if (post.getStatus() != PostStatus.DRAFT) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Attachments can be modified only for drafts");
        }

        return post;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File is required");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File is too large (max 20MB)");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File type is not allowed");
        }
    }

    private String normalizeContentType(String ct) {
        if (ct == null) return "application/octet-stream";
        return ct.trim().toLowerCase();
    }

    private AttachmentType detectType(String contentType) {
        if (contentType.startsWith("image/")) return AttachmentType.IMAGE;
        if (contentType.startsWith("video/")) return AttachmentType.VIDEO;
        return AttachmentType.FILE;
    }

    private String trimTo(String s, int max) {
        String t = s.trim();
        return (t.length() <= max) ? t : t.substring(0, max);
    }

}
