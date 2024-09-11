package com.harman.ignite.cache.exception;

/**
 * Custom exception for errors from ignite-cache library.
 */
public class IgniteCacheException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IgniteCacheException(String message) {
        super(message);
    }

    public IgniteCacheException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
