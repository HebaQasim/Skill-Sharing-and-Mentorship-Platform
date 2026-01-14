package com.company.skillplatform.common.cache;

import java.util.UUID;

public final class CacheKeys {
    private CacheKeys() {}

    public static String notificationsTop(UUID userId) {
        return "notifications:top20:" + userId;
    }

    public static String notificationsUnreadCount(UUID userId) {
        return "notifications:unreadCount:" + userId;
    }

    public static String postFeed(long stamp, int limit, String cursor) {
        String c = (cursor == null || cursor.isBlank()) ? "first" : cursor;
        return "postFeed:v" + stamp + ":l" + limit + ":c" + c;
    }

    public static String postLikers(long stamp, String postId, int limit, String cursor) {
        return "post:" + postId + ":likers:" + stamp + ":l=" + limit + ":c=" + (cursor == null ? "" : cursor);
    }

    public static String postCommentsTop20(UUID postId, long stamp) {
        return "postComments:top20:" + postId + ":v" + stamp;
    }

    public static String employeesColleaguesTop20(long stamp, String dept) {
        return "employees:colleagues:top20:v" + stamp + ":dept=" + dept;
    }

    public static String employeesDirectoryTop20(long stamp) {
        return "employees:directory:top20:v" + stamp;
    }

    public static String profilePostsShared(long stamp, String profileUserId, int limit, String cursor) {
        String c = (cursor == null || cursor.isBlank()) ? "first" : cursor;
        return "profilePosts:shared:v" + stamp + ":u" + profileUserId + ":l" + limit + ":c" + c;
    }

    public static String adminAuditTop20(long stamp) {
        return "adminAudit:top20:v" + stamp;
    }


}
