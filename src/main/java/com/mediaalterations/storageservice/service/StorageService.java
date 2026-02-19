package com.mediaalterations.storageservice.service;

import com.mediaalterations.storageservice.dto.OutputPathResponse;
import com.mediaalterations.storageservice.entity.Storage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StorageService {
    String store(MultipartFile file,String userId) throws IOException;

    FileSystemResource download(String filePath, String userId) throws Exception;

    String getPathFromStorageId(String storageId, String userId) throws Exception;

    OutputPathResponse generateOutputPath(String filename, String contentType, String userId) throws IOException;

    Storage getStorageDetails(String storageId);

    List<String> deleteStorage(List<String> storageIds, String userId);
}
