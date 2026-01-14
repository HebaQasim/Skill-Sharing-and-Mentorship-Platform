package com.company.skillplatform.session.cursor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public final class FeedbackCursorCodec {

    private FeedbackCursorCodec() {}

    public static String encode(FeedbackCursor c) {
        String raw = c.createdAt() + "|" + c.id();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FeedbackCursor decode(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] p = raw.split("\\|");
            return new FeedbackCursor(LocalDateTime.parse(p[0]), UUID.fromString(p[1]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }
}
