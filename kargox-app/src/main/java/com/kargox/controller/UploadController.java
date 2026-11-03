package com.kargox.controller;

import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.common.UploadResponse;
import com.kargox.service.FileStorageService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
@Tag(name = "Uploads", description = "Image uploads for goods photos and profile pictures")
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UploadResponse>> upload(@RequestParam("file")
                                                              MultipartFile file,
                                                              @RequestParam(defaultValue = "misc")
                                                              String type) {

        String subfolder = switch (type) {
            case "goods" -> "goods";
            case "profile" -> "profiles";
            default -> "misc";
        };
        String url = fileStorageService.store(file, subfolder);

        return ResponseEntity.ok(ApiResponse.ok(new UploadResponse(url)));
    }
}
