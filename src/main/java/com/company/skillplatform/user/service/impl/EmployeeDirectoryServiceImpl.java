package com.company.skillplatform.user.service.impl;

import com.company.skillplatform.common.cache.CacheKeys;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.common.web.PageRequests;
import com.company.skillplatform.post.dto.PostCardCached;
import com.company.skillplatform.skill.dto.SkillResponse;
import com.company.skillplatform.skill.enums.SkillSort;
import com.company.skillplatform.skill.repository.SkillRepository;
import com.company.skillplatform.user.cursor.*;
import com.company.skillplatform.user.dto.EmployeeCardResponse;
import com.company.skillplatform.user.dto.MyProfileResponse;
import com.company.skillplatform.user.dto.MyProfileWithPostsResponse;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.EmployeeRow;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.user.service.EmployeeDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeDirectoryServiceImpl implements EmployeeDirectoryService {

    public static final String EMP_DIR_STAMP = "employeeDirectory";
    public static final String EMP_DIR_CACHE = "employeeDirectoryCache";

    private final UserRepository userRepository;
    private final CacheStampService cacheStampService;
    private final StorageService storageService;
private final SkillRepository skillRepository;
    @Override
    public CursorPageResponse<EmployeeCardResponse> colleagues(UUID viewerUserId, Integer limit, String cursor, String q) {

        User viewer = userRepository.findById(viewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        int size = normalizeLimit(limit);
        String dept = viewer.getDepartment();

        boolean firstPage = (cursor == null || cursor.isBlank());
        boolean noSearch = (q == null || q.isBlank());
        if (firstPage && noSearch && size == 20) {
            long stamp = cacheStampService.getStamp(EMP_DIR_STAMP);
            return colleaguesTop20Cached(stamp, dept);
        }


        return colleaguesUncached(dept, size, cursor, q);
    }

    @Cacheable(
            cacheNames = EMP_DIR_CACHE,
            key = "T(com.company.skillplatform.common.cache.CacheKeys).employeesColleaguesTop20(#stamp, #dept)"
    )
    public CursorPageResponse<EmployeeCardResponse> colleaguesTop20Cached(long stamp, String dept) {
        return colleaguesUncached(dept, 20, null, null);
    }

    private CursorPageResponse<EmployeeCardResponse> colleaguesUncached(String dept, int size, String cursor, String q) {

        var pageable = PageRequest.of(0, size + 1);

        List<EmployeeRow> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = userRepository.colleaguesFirstPage(dept, q, pageable);
        } else {
            ColleagueCursor c = ColleagueCursorCodec.decode(cursor);
            rows = userRepository.colleaguesNextPage(dept, q, c.lastName(), c.firstName(), c.id(), pageable);
        }

        return mapCursorPage(rows, size, true);
    }

    @Override
    public CursorPageResponse<EmployeeCardResponse> directory(Integer limit, String cursor, String q) {

        int size = normalizeLimit(limit);

        boolean firstPage = (cursor == null || cursor.isBlank());
        boolean noSearch = (q == null || q.isBlank());


        if (firstPage && noSearch && size == 20) {
            long stamp = cacheStampService.getStamp(EMP_DIR_STAMP);
            return directoryTop20Cached(stamp);
        }
    return directoryUncached(size, cursor, q);
    }


    @Cacheable(
            cacheNames = EMP_DIR_CACHE,
            key = "T(com.company.skillplatform.common.cache.CacheKeys).employeesDirectoryTop20(#stamp)"
    )
    public CursorPageResponse<EmployeeCardResponse> directoryTop20Cached(long stamp) {
        return directoryUncached(20, null, null);
    }

    private CursorPageResponse<EmployeeCardResponse> directoryUncached(int size, String cursor, String q) {

        var pageable = PageRequest.of(0, size + 1);

        List<EmployeeRow> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = userRepository.directoryFirstPage(q, pageable);
        } else {
            DirectoryCursor c = DirectoryCursorCodec.decode(cursor);
            rows = userRepository.directoryNextPage(q, c.department(), c.lastName(), c.firstName(), c.id(), pageable);
        }

        return mapCursorPage(rows, size, false);
    }

    // -------- helpers --------

    private CursorPageResponse<EmployeeCardResponse> mapCursorPage(List<EmployeeRow> rows, int size, boolean colleagues) {
        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        var items = rows.stream().map(this::toCard).toList();

        String nextCursor = null;
        if (hasNext && !rows.isEmpty()) {
            EmployeeRow last = rows.get(rows.size() - 1);
            if (colleagues) {
                nextCursor = ColleagueCursorCodec.encode(
                        new ColleagueCursor(last.getLastName(), last.getFirstName(), last.getId())
                );
            } else {
                nextCursor = DirectoryCursorCodec.encode(
                        new DirectoryCursor(last.getDepartment(), last.getLastName(), last.getFirstName(), last.getId())
                );
            }
        }

        return new CursorPageResponse<>(items, nextCursor);
    }

    private EmployeeCardResponse toCard(EmployeeRow r) {
        String url = (r.getProfileImageUrl() == null) ? null : storageService.signedUrl(r.getProfileImageUrl());
        return new EmployeeCardResponse(
                r.getId(),
                r.getFirstName() + " " + r.getLastName(),
                r.getDepartment(),
                r.getJobTitle(),
                r.getHeadline(),
                url

        );
    }

    private int normalizeLimit(Integer limit) {
        int v = (limit == null) ? 20 : limit;
        v = Math.max(5, v);
        v = Math.min(50, v);
        return v;
    }
}
