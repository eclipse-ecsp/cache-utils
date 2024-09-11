package com.harman.ignite.cache.exception;

/**
 * Custom exception for file not found.
 */
public class FileNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileNotFoundException(String message) {
        super(message);
    }
}
