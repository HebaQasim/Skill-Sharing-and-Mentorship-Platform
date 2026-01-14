package com.company.skillplatform.notification.cursor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public final class NotificationCursorCodec {

    private NotificationCursorCodec() {}

    public static String encode(NotificationCursor cursor) {
        String raw = cursor.createdAt() + "|" + cursor.id();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static NotificationCursor decode(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|");
            return new NotificationCursor(LocalDateTime.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }
}
