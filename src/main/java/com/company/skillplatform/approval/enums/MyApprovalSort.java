package com.company.skillplatform.approval.enums;

import com.company.skillplatform.common.web.SortOption;
import org.springframework.data.domain.Sort;

public enum MyApprovalSort implements SortOption {
    NEWEST,
    OLDEST;

    @Override
    public Sort toSort() {
        return switch (this) {
            case NEWEST -> Sort.by("createdAt").descending();
            case OLDEST -> Sort.by("createdAt").ascending();
        };
    }
}

