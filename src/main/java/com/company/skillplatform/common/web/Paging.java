package com.company.skillplatform.common.web;

import com.company.skillplatform.common.dto.PagedResponse;
import org.springframework.data.domain.Page;

public final class Paging {

    private Paging() {}

    public static <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}

