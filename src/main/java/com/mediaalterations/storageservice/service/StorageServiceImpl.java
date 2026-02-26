package com.mediaalterations.storageservice.service;

import com.mediaalterations.storageservice.dto.OutputPathResponse;
import com.mediaalterations.storageservice.dto.StorageDeleteProjection;
import com.mediaalterations.storageservice.dto.StorageDto;
import com.mediaalterations.storageservice.entity.Storage;
import com.mediaalterations.storageservice.exceptions.StorageNotFoundException;
import com.mediaalterations.storageservice.exceptions.StorageOperationException;
import com.mediaalterations.storageservice.exceptions.UnauthorizedStorageAccessException;
import com.mediaalterations.storageservice.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {

        private final S3Client s3Client;
        private final StorageRepository storageRepository;

        // @Value("${app.paths.dir.uploads}")
        // private String baseUploadPath;
        //
        // @Value("${app.paths.dir.downloads}")
        // private String baseDownloadPath;

        @Value("${garage.bucket.uploads}")
        private String uploadsBucket;

        @Value("${garage.bucket.downloads}")
        private String downloadsBucket;

        // ================= STORE =================

        private String getReadableFileSize(long size) {
                if (size <= 0)
                        return "0 B";
                final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
                int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
                return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
        }

        @Override
        public String store(MultipartFile file, String userId) {

                log.info("Upload request received. userId={}, originalFilename={}",
                                userId, file.getOriginalFilename());

                if (file.isEmpty()) {
                        throw new StorageOperationException("Uploaded file is empty", null);
                }

                if (!file.getContentType().startsWith("video/")
                                && !file.getContentType().startsWith("audio/")) {
                        throw new StorageOperationException("Only audio/video allowed", null);
                }

                try {
                        String objectKey = UUID.randomUUID() + "-" +
                                        StringUtils.cleanPath(file.getOriginalFilename());

                        // Upload to Garage
                        s3Client.putObject(
                                        PutObjectRequest.builder()
                                                        .bucket(uploadsBucket)
                                                        .key(objectKey)
                                                        .contentType(file.getContentType())
                                                        .build(),
                                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

                        // Local file storage
                        /*
                         * Path basePath = Path.of(baseUploadPath);
                         * Files.createDirectories(basePath);
                         * String filename = UUID.randomUUID().toString();
                         * String finalName = filename + "-" +
                         * StringUtils.cleanPath(file.getOriginalFilename());
                         * 
                         * 
                         * Path target = basePath.resolve(finalName);
                         * Files.copy(file.getInputStream(), target);
                         */

                        String fileSize = getReadableFileSize(file.getSize());
                        String duration = "";
                        String fileType = file.getOriginalFilename().contains(".")
                                        ? file.getOriginalFilename()
                                                        .substring(file.getOriginalFilename().lastIndexOf(".") + 1)
                                        : "unknown";
                        String mediaType = file.getContentType().startsWith("video/") ? "video" : "audio";

                        Storage storageEntity = storageRepository.save(
                                        new Storage(
                                                        userId,
                                                        // target.toString(),
                                                        objectKey,
                                                        objectKey,
                                                        mediaType,
                                                        fileSize, duration, fileType, false));

                        log.info("Stored in Garage. storageId={}, key={}, size={}", storageEntity.getId(), objectKey,
                                        fileSize);

                        return storageEntity.getId().toString();

                } catch (IOException e) {
                        log.error("File upload failed. userId={}, file={}",
                                        userId, file.getOriginalFilename(), e);
                        throw new StorageOperationException("Failed to store file", e);
                }
        }

        // ================= DOWNLOAD =================

        @Override
        public FileSystemResource download(String storageId, String userId) {

                log.info("Download request. storageId={}, userId={}", storageId, userId);

                Storage storage = storageRepository.findById(UUID.fromString(storageId))
                                .orElseThrow(() -> {
                                        log.warn("Download failed. File not found. storageId={}", storageId);
                                        return new StorageNotFoundException("File not found");
                                });

                if (!storage.getUserId().equals(userId)) {
                        log.warn("Unauthorized download attempt. storageId={}, requestedBy={}",
                                        storageId, userId);
                        throw new UnauthorizedStorageAccessException("Unauthorized to download this file");
                }

                if (!storage.isDownloadable()) {
                        log.warn("File is not marked downloadable. storageId={}", storageId);
                        throw new StorageOperationException("File is not available for download", null);
                }

                // FileSystemResource resource = new FileSystemResource(storage.getPath());
                //
                // if (!resource.exists()) {
                // log.error("Physical file missing on disk. storageId={}, path={}",
                // storageId, storage.getPath());
                // throw new StorageNotFoundException("File not found");
                // }

                try {
                        // Stream from Garage to temp file
                        ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(
                                        GetObjectRequest.builder()
                                                        .bucket(downloadsBucket)
                                                        .key(storage.getPath()) // path column now holds the object key
                                                        .build());

                        Path tempFile = Files.createTempFile("download-", storage.getFileName());
                        Files.write(tempFile, object.asByteArray());
                        tempFile.toFile().deleteOnExit();

                        log.info("File download permitted. storageId={}, userId={}", storageId, userId);

                        return new FileSystemResource(tempFile);

                } catch (Exception e) {
                        throw new StorageNotFoundException("Failed to retrieve file from storage");
                }

        }

        // ================= GET PATH =================

        @Override
        public String getPathFromStorageId(String storageId, String userId) {

                log.debug("Fetching path for storageId={}, userId={}", storageId, userId);

                return storageRepository.getPathFromId(UUID.fromString(storageId))
                                .orElseThrow(() -> {
                                        log.warn("Path not found for storageId={}", storageId);
                                        return new StorageNotFoundException("File path not found");
                                });
        }

        // ================= GENERATE OUTPUT =================

        @Override
        public OutputPathResponse generateOutputPath(
                        String orgFileName,
                        String contentType,
                        String fileType,
                        String duration,
                        String userId) {

                log.info("Generating output path. userId={}, originalFileName={}",
                                userId, orgFileName);

                // Path basePath = Path.of(baseDownloadPath);
                // Files.createDirectories(basePath);
                //
                // String filename = UUID.randomUUID().toString();
                // String finalName = filename + "." + contentType;
                //
                // Path target = basePath.resolve(finalName);

                String objectKey = UUID.randomUUID() + "." + contentType;
                Storage storageEntity = storageRepository.save(
                                new Storage(
                                                userId,
                                                objectKey,
                                                objectKey,
                                                // target.toString(),
                                                // finalName,
                                                // since file duration will be updated later on
                                                contentType, "0 KB", duration, fileType,
                                                false));

                log.info("Output path generated. storageId={}, filename={}",
                                storageEntity.getId(), objectKey);

                return new OutputPathResponse(
                                storageEntity.getId().toString(),
                                objectKey);

        }

        // ================= GET DETAILS =================

        @Override
        public Storage getStorageDetails(String storageId) {

                log.debug("Fetching storage details. storageId={}", storageId);

                return storageRepository.findById(UUID.fromString(storageId))
                                .orElseThrow(() -> {
                                        log.warn("Storage not found. storageId={}", storageId);
                                        return new StorageNotFoundException("No file found");
                                });
        }

        // ================= DELETE =================

        @Override
        @Transactional
        public List<String> deleteStorage(List<String> storageIds, String userId) {

                log.info("Delete request. userId={}, totalFiles={}",
                                userId, storageIds.size());

                List<UUID> uuids = storageIds.stream()
                                .map(UUID::fromString)
                                .toList();

                List<StorageDeleteProjection> records = storageRepository.findForDeletion(uuids, userId);

                if (records.isEmpty()) {
                        log.warn("No valid files found for deletion. userId={}", userId);
                        throw new StorageNotFoundException("No valid files found.");
                }

                for (StorageDeleteProjection record : records) {
                        // try {
                        // Files.deleteIfExists(Paths.get(record.path()));
                        // log.info("Deleted file from disk. fileName={}", record.fileName());
                        // } catch (IOException e) {
                        // log.error("Failed to delete file from disk. fileName={}",
                        // record.fileName(), e);
                        // throw new StorageOperationException(
                        // "Failed to delete file: " + record.fileName(), e);
                        // }

                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                        .bucket(uploadsBucket)
                                        .key(record.path())
                                        .build());
                        log.info("Deleted from Garage. key={}", record.path());
                }

                storageRepository.deleteByIdsAndUserId(uuids, userId);

                log.info("Database records deleted successfully. userId={}", userId);

                return records.stream()
                                .map(StorageDeleteProjection::fileName)
                                .toList();
        }

        @Override
        public void makeFileDownloadable(String storageId) {
                log.info("Marking file as downloadable. storageId={}", storageId);

                Storage storage = storageRepository.findById(UUID.fromString(storageId))
                                .orElseThrow(() -> {
                                        log.warn("Storage not found. storageId={}", storageId);
                                        return new StorageNotFoundException("File not found");
                                });

                if (storage.isDownloadable()) {
                        log.info("File already marked as downloadable. storageId={}", storageId);
                        return;
                }

                storage.setDownloadable(true);
                storageRepository.save(storage);

                log.info("File marked as downloadable. storageId={}", storageId);
        }

        @Override
        public List<StorageDto> getUserUploadedMedia(String userId) {
                log.info("Fetching uploaded media for user.");

                List<Storage> storages = storageRepository.findByUserIdAndMediaTypeIn(
                                userId, List.of("audio", "video"));

                return storages.stream()
                                .map(s -> new StorageDto(
                                                s.getId().toString(),
                                                userId,
                                                s.getPath(),
                                                s.getFileName(),
                                                s.getMediaType(),
                                                s.getFileSize(),
                                                s.getDuration(),
                                                s.getFileType(),
                                                s.isDownloadable()))
                                .toList();
        }
}
