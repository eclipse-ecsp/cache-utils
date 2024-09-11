package com.harman.ignite.cache.exception;

/**
 * Custom exception thrown while decoding the value from redis.
 */
public class DecodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DecodeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
