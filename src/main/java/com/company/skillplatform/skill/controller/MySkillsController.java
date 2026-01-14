package com.company.skillplatform.skill.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.PagedResponse;
import com.company.skillplatform.common.web.PageRequests;
import com.company.skillplatform.common.web.Paging;
import com.company.skillplatform.skill.dto.AddSkillRequest;
import com.company.skillplatform.skill.dto.SkillResponse;
import com.company.skillplatform.skill.enums.SkillSort;
import com.company.skillplatform.skill.service.MySkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/skills")
@RequiredArgsConstructor
public class MySkillsController {

    private final MySkillService mySkillService;

    @PostMapping
    public ResponseEntity<SkillResponse> add(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody AddSkillRequest request
    ) {
        return ResponseEntity.ok(mySkillService.addSkill(user.getId(), request.name()));
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID skillId
    ) {
        mySkillService.removeSkill(user.getId(), skillId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<SkillResponse>> search(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(mySkillService.searchActiveSkills(q));
    }
    @GetMapping
    public ResponseEntity<PagedResponse<SkillResponse>> mySkills(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "NAME_ASC") SkillSort sort
    ) {
        Pageable pageable = PageRequests.of(page, size, sort.toSort());
        var result = mySkillService.mySkills(user.getId(), pageable);
        return ResponseEntity.ok(Paging.toPagedResponse(result));
    }
}

