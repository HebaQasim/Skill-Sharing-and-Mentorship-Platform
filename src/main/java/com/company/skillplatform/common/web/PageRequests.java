package com.company.skillplatform.common.web;

import org.springframework.data.domain.*;

public final class PageRequests {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private PageRequests() {}

    public static Pageable of(Integer page, Integer size, Sort sort) {
        int safePage = (page == null) ? 0 : Math.max(page, 0);
        int requestedSize = (size == null) ? DEFAULT_SIZE : size;
        int safeSize = Math.min(Math.max(requestedSize, 1), MAX_SIZE);

        return PageRequest.of(safePage, safeSize, sort == null ? Sort.unsorted() : sort);
    }
}

