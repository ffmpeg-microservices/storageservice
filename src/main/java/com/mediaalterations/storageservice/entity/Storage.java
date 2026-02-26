package com.mediaalterations.storageservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userId;

    // this becomes the object key when using S3
    private String path;

    private String fileName;
    private String mediaType;
    private String fileSize;
    private String duration;
    private String fileType;

    private boolean isDownloadable;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Storage(String userId, String path, String fileName, String mediaType, String fileSize, String duration,
            String fileType, boolean isDownloadable) {
        this.userId = userId;
        this.path = path;
        this.fileName = fileName;
        this.mediaType = mediaType;
        this.fileSize = fileSize;
        this.duration = duration;
        this.fileType = fileType;
        this.isDownloadable = isDownloadable;
    }

}
