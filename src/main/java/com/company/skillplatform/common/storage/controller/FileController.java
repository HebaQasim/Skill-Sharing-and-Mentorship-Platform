package com.company.skillplatform.common.storage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.file.*;

@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${app.storage.local.root:uploads}")
    private String rootDir;

    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatch = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String relative = new AntPathMatcher().extractPathWithinPattern(bestMatch, path);

        Path filePath = Paths.get(rootDir).resolve(relative).normalize();

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(resource);
    }
}
