/*
 ********************************************************************************
 * COPYRIGHT (c) 2024 Harman International Industries, Inc                      *
 *                                                                              *
 * All rights reserved                                                          *
 *                                                                              *
 * This software embodies materials and concepts which are                      *
 * confidential to Harman International Industries, Inc. and is                 *
 * made available solely pursuant to the terms of a written license             *
 * agreement with Harman International Industries, Inc.                         *
 *                                                                              *
 * Designed and Developed by Harman International Industries, Inc.              *
 *------------------------------------------------------------------------------*
 * MODULE OR UNIT: ignite-cache                                                 *
 ********************************************************************************
 */

package com.harman.ignite.cache;

/**
 * Captures the options for putting an entity into cache.
 *
 * @author ssasidharan
 * @param <T> Any type that implements IgniteEntity (enforced by IgniteCache contract)
 *
 */
public class PutEntityRequest<T> extends PutEntityBaseRequest<T> {

    /**
     * Optional. If greater than 0 then ttl will be applied
     */
    private long ttlMs = -1L;

    /**
     * Optional. If value is non-null then put becomes a compare and set operation
     * ie, put will be applied only if the existing value in
     * cache should match the value here.
     */
    private T expectedValue;

    public PutEntityRequest() {
        //default constructor
    }

    /**
     * If greater than 0 then ttl will be applied.
     */
    public PutEntityRequest<T> withTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
        return this;
    }

    /**
     * If value is non-null then put becomes a compare and set operation
     * ie, put will be applied only if the existing value in cache should
     * match the value here.
     *
     * @param expectedValue - null is valid. If non-null then put becomes a compare and set operation
     * @return this
     */
    public PutEntityRequest<T> ifCurrentMatches(T expectedValue) {
        this.expectedValue = expectedValue;
        return this;
    }

    public long getTtlMs() {
        return ttlMs;
    }

    public T getExpectedValue() {
        return expectedValue;
    }

}
