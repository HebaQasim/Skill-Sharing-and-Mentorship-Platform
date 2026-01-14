package com.company.skillplatform.admin.audit.cursor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public final class AdminAuditCursorCodec {

    private AdminAuditCursorCodec() {}

    public static String encode(AdminAuditCursor c) {
        String raw = c.createdAt() + "|" + c.id();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static AdminAuditCursor decode(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] p = raw.split("\\|");
            return new AdminAuditCursor(LocalDateTime.parse(p[0]), UUID.fromString(p[1]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }
}
