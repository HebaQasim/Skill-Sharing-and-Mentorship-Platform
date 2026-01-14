package com.company.skillplatform.skill.enums;

import com.company.skillplatform.common.web.SortOption;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;

public enum SkillSort implements SortOption {
    NAME_ASC,
    NAME_DESC,
    NEWEST,
    OLDEST;

    @Override
    public Sort toSort() {

        return switch (this) {
            case NAME_ASC -> JpaSort.unsafe("s.name").ascending();
            case NAME_DESC -> JpaSort.unsafe("s.name").descending();
            case NEWEST -> JpaSort.unsafe("s.createdAt").descending();
            case OLDEST -> JpaSort.unsafe("s.createdAt").ascending();
        };
    }
}
