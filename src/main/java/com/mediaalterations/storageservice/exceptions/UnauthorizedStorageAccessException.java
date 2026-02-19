package com.mediaalterations.storageservice.exceptions;


public class UnauthorizedStorageAccessException extends RuntimeException {
    public UnauthorizedStorageAccessException(String message) {
        super(message);
    }
}