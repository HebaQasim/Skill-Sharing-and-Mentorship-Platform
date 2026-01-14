package com.company.skillplatform.user.cursor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class ColleagueCursorCodec {
    private ColleagueCursorCodec() {}

    public static String encode(ColleagueCursor c) {
        String raw = safe(c.lastName()) + "|" + safe(c.firstName()) + "|" + c.id();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static ColleagueCursor decode(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] p = raw.split("\\|", -1);
            return new ColleagueCursor(p[0], p[1], UUID.fromString(p[2]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    private static String safe(String v) { return v == null ? "" : v; }
}
