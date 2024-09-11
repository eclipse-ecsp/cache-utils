package com.harman.ignite.cache.exception;

/**
 * Custom exception in case of batch processing failure.
 */
public class RedisBatchProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RedisBatchProcessingException(String message) {
        super(message);
    }

}
