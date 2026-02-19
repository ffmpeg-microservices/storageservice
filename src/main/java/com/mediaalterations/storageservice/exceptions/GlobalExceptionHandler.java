package com.mediaalterations.storageservice.exceptions;

import com.mediaalterations.storageservice.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StorageNotFoundException.class)
    public ResponseEntity<ApiError> handleException(StorageNotFoundException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.builder()
                        .status(400)
                        .errorMessage(ex.getMessage())
                        .errorClass(ex.getClass().getName())
                        .build());
    }

    @ExceptionHandler(StorageOperationException.class)
    public ResponseEntity<ApiError> handleException(StorageOperationException ex){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.builder()
                        .status(500)
                        .errorMessage(ex.getMessage())
                        .errorClass(ex.getClass().getName())
                        .build());
    }
    @ExceptionHandler(UnauthorizedStorageAccessException.class)
    public ResponseEntity<ApiError> handleException(UnauthorizedStorageAccessException ex){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.builder()
                        .status(500)
                        .errorMessage(ex.getMessage())
                        .errorClass(ex.getClass().getName())
                        .build());
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleException(MaxUploadSizeExceededException ex){
        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ApiError.builder()
                        .status(413)
                        .errorMessage(ex.getMessage())
                        .errorClass(ex.getClass().getName())
                        .build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.builder()
                        .status(500)
                        .errorMessage(ex.getMessage())
                        .errorClass(ex.getClass().getName())
                        .build());
    }
}
