package com.company.skillplatform.common.storage.impl;

import com.company.skillplatform.common.storage.StorageResult;
import com.company.skillplatform.common.storage.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local.root:uploads}")
    private String rootDir;

    @Value("${app.storage.local.public-base:/files}")
    private String publicBase;

    @Override
    public StorageResult store(String folder, MultipartFile file) {
        try {
            String ext = getSafeExtension(file.getOriginalFilename());
            String name = UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);

            String key = folder.replace("\\", "/") + "/" + name;
            Path target = Paths.get(rootDir).resolve(key).normalize();

            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return new StorageResult(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file locally", e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path target = Paths.get(rootDir).resolve(storageKey).normalize();
            Files.deleteIfExists(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete local file", e);
        }
    }

    @Override
    public String signedUrl(String storageKey) {
        // local dev: URL مباشرة
        return publicBase + "/" + storageKey;
    }

    private String getSafeExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) return "";
        String cleaned = originalFilename.trim();
        int dot = cleaned.lastIndexOf('.');
        if (dot < 0 || dot == cleaned.length() - 1) return "";
        return cleaned.substring(dot + 1).toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
