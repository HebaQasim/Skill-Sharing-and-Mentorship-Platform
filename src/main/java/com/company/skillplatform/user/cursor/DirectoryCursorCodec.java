package com.company.skillplatform.user.cursor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class DirectoryCursorCodec {
    private DirectoryCursorCodec() {}

    public static String encode(DirectoryCursor c) {
        String raw = safe(c.department()) + "|" + safe(c.lastName()) + "|" + safe(c.firstName()) + "|" + c.id();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static DirectoryCursor decode(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] p = raw.split("\\|", -1);
            return new DirectoryCursor(p[0], p[1], p[2], UUID.fromString(p[3]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    private static String safe(String v) { return v == null ? "" : v; }
}

