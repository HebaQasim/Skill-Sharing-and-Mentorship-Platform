package com.company.skillplatform.post.listener;

import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.post.event.PostContentUpdatedEvent;
import com.company.skillplatform.post.repository.PostCommentRepository;
import com.company.skillplatform.post.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.*;

@Component
@RequiredArgsConstructor
public class PostContentUpdatedNotificationListener {

    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final NotificationRepository notificationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PostContentUpdatedEvent e) {
        UUID postId = e.postId();
        UUID editorId = e.editorUserId();

        List<UUID> likers = postLikeRepository.findDistinctLikerUserIds(postId);
        List<UUID> commenters = postCommentRepository.findDistinctCommenterUserIds(postId);


        Set<UUID> recipients = new HashSet<>();
        recipients.addAll(likers);
        recipients.addAll(commenters);


        recipients.remove(editorId);

        if (recipients.isEmpty()) return;

        List<Notification> notifications = recipients.stream()
                .map(uid -> Notification.builder()
                        .recipientUserId(uid)
                        .type(NotificationType.POST_UPDATED)
                        .title("Post updated")
                        .message("A post you interacted with has been updated.")
                        .link("/posts/" + postId)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }
}
