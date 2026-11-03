package com.kargox.service.impl;

import com.kargox.exception.BadRequestException;
import com.kargox.service.FileStorageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_FILE_SIZE_BYTES = 8 * 1024 * 1024; // 8MB

    @Value("${kargox.uploads.directory}")
    private String uploadsDirectory;

    @Override
    public String store(MultipartFile file, String subfolder) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file was uploaded");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Image must be smaller than 8MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPEG, PNG, WEBP or GIF images are allowed");
        }

        try {
            Path targetDir = Path.of(uploadsDirectory, subfolder);
            Files.createDirectories(targetDir);

            String extension = switch (contentType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/gif" -> ".gif";
                default -> "";
            };
            String filename = UUID.randomUUID() + extension;
            Path targetFile = targetDir.resolve(filename);
            file.transferTo(targetFile);

            return "/uploads/" + subfolder + "/" + filename;
        } catch (IOException e) {
            throw new BadRequestException("Could not save the uploaded file. Please try again.");
        }
    }
}
