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
 * Represents the options for putting a key value string to cache.
 *
 * @author ssasidharan
 */
public class PutStringRequest {
    /**
     * Mandatory attribute.
     */
    private String key;

    /**
     * Mandatory attribute.
     */
    private String value;

    /**
     * Optional. If greater than 0 then ttl will be applied.
     */
    private long ttlMs = -1L;

    /**
     * Optional. If value is non-null then put becomes a compare and set operation
     * ie, put will be applied only if the existing value in
     * cache should match the value here.
     */
    private String expectedValue;

    /**
     * Optional attribute. The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     */
    private String mutationId;

    private boolean namespaceEnabled;

    public PutStringRequest() {
        this.namespaceEnabled = true;
    }

    public PutStringRequest withKey(String key) {
        this.key = key;
        return this;
    }

    public PutStringRequest withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * If greater than 0 then ttl will be applied.
     *
     * @param ttlMs - time to live in milliseconds
     * @return this
     */
    public PutStringRequest withTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
        return this;
    }

    /**
     * If expectedValue is non-null then put becomes a compare and set operation
     * ie, put will be applied only if the existing value in cache
     * should match the value here.
     *
     * @param expectedValue - set expected value
     */
    public PutStringRequest ifCurrentMatches(String expectedValue) {
        this.expectedValue = expectedValue;
        return this;
    }

    /**
     * The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     *
     * @param mutationId
     *         - null is valid.
     * @return this
     */
    public PutStringRequest withMutationId(String mutationId) {
        this.mutationId = mutationId;
        return this;
    }

    public PutStringRequest withNamespaceEnabled(boolean namespaceEnabled) {
        this.namespaceEnabled = namespaceEnabled;
        return this;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public long getTtlMs() {
        return ttlMs;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public String getMutationId() {
        return mutationId;
    }

    public boolean getNamespaceEnabled() {
        return namespaceEnabled;
    }

}