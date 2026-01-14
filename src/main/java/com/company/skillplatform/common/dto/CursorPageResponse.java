package com.company.skillplatform.common.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor
) {}
