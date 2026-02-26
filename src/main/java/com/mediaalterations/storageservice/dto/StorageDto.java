package com.mediaalterations.storageservice.dto;

public record StorageDto(
        String storageId,
        String userId,
        String path,
        String fileName,
        String mediaType,
        String fileSize,
        String duration,
        String fileType,
        boolean isDownloadable) {

}
