package com.k8stoc4.controller.writer;

public class FileWriteException extends RuntimeException {
    public FileWriteException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
