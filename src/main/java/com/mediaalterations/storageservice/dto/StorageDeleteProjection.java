package com.mediaalterations.storageservice.dto;

import java.util.UUID;

public record StorageDeleteProjection(
        UUID id,
        String path,
        String fileName
) {
}
