package com.mediaalterations.storageservice.service;

import com.mediaalterations.storageservice.dto.OutputPathResponse;
import com.mediaalterations.storageservice.dto.StorageDto;
import com.mediaalterations.storageservice.entity.Storage;

import org.jspecify.annotations.Nullable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StorageService {
    String store(MultipartFile file, String userId) throws IOException;

    FileSystemResource download(String filePath, String userId) throws Exception;

    String getPathFromStorageId(String storageId, String userId) throws Exception;

    OutputPathResponse generateOutputPath(
            String orgFileName,
            String contentType,
            String fileType,
            String duration,
            String userId) throws IOException;

    Storage getStorageDetails(String storageId);

    List<String> deleteStorage(List<String> storageIds, String userId);

    void makeFileDownloadable(String storageId);

    List<StorageDto> getUserUploadedMedia(String userId);

    String[] storeMultipleFiles(MultipartFile[] files, String userId);

    Map<String, String> getAllPathsFromStorageIds(String[] storageIds, String userId);
}
