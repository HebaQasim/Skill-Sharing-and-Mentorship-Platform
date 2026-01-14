package com.company.skillplatform.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StorageResult store(String folder, MultipartFile file);

    void delete(String storageKey);

    String signedUrl(String storageKey);
}

