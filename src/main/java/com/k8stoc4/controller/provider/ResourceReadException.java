package com.k8stoc4.controller.provider;

public class ResourceReadException extends RuntimeException {
    public ResourceReadException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
