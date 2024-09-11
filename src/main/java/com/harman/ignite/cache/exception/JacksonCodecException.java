package com.harman.ignite.cache.exception;

/**
 * Custom Exception thrown in case of failing to load the class configured against the property: ignite.codec.class. 
 */
public class JacksonCodecException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JacksonCodecException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
