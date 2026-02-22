package com.mediaalterations.storageservice.controller;

import com.mediaalterations.storageservice.dto.OutputPathResponse;
import com.mediaalterations.storageservice.entity.Storage;
import com.mediaalterations.storageservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("user_id") String userId) throws IOException {
        String storageId = storageService.store(file, userId);
        return ResponseEntity.ok(storageId);
    }

    @GetMapping("/download/{storage_id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable("storage_id") String storageId,
            @RequestHeader("user_id") String userId) throws Exception {
        FileSystemResource resource = storageService.download(storageId, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/getPath/{id}")
    public ResponseEntity<String> getPathFromStorageId(
            @PathVariable("id") String storageId,
            @RequestHeader("user_id") String userId) throws Exception {
        String path = storageService.getPathFromStorageId(storageId, userId);
        return ResponseEntity.ok(path);
    }

    @GetMapping("/getStorageDetails/{id}")
    public ResponseEntity<Storage> getStorageDetails(
            @PathVariable("id") String storageId) throws Exception {
        return ResponseEntity.ok(storageService.getStorageDetails(storageId));
    }

    @GetMapping("/generateOutputPath/{filename}/{contentType}")
    public ResponseEntity<OutputPathResponse> generateOutputPath(
            @PathVariable("filename") String filename,
            @PathVariable("contentType") String contentType,
            @RequestHeader("user_id") String userId) throws Exception {
        OutputPathResponse path = storageService.generateOutputPath(filename, contentType, userId);
        return ResponseEntity.ok(path);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<String>> deleteStorage(
            @RequestBody List<String> storageIds,
            @RequestHeader("user_id") String userId) {

        return ResponseEntity.ok(storageService.deleteStorage(storageIds, userId));
    }

}
