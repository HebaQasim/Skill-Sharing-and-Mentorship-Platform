package com.company.skillplatform.common.util;

public final class Texts {

    private Texts() {}

    public static String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
