package com.mediaalterations.storageservice.exceptions;

public class StorageNotFoundException extends RuntimeException {
    public StorageNotFoundException(String message) {
        super(message);
    }
}