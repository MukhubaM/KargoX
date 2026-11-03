package com.kargox.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // Stores an uploaded file under the given subfolder
    String store(MultipartFile file, String subfolder);
}
