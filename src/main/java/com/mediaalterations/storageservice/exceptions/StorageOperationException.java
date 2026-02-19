package com.mediaalterations.storageservice.exceptions;

import java.io.IOException;

public class StorageOperationException extends RuntimeException {
    public StorageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
