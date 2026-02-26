package com.mediaalterations.storageservice.dto;

import java.time.LocalDateTime;

public record StorageDto(
                String storageId,
                String userId,
                String path,
                String fileName,
                String mediaType,
                String fileSize,
                String duration,
                String fileType,
                LocalDateTime uploadedDate) {

}
