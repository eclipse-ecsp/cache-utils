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
 */
public abstract class PutEntityBaseRequest<T> {
    /**
     * Mandatory attribute.
     */
    private String key;

    /**
     * Mandatory attribute.
     */
    private T value;

    /**
     * Optional attribute. The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     */
    private String mutationId;

    private boolean namespaceEnabled;

    protected PutEntityBaseRequest() {
        this.namespaceEnabled = true;
    }

    public PutEntityBaseRequest<T> withKey(String key) {
        this.key = key;
        return this;
    }

    public PutEntityBaseRequest<T> withValue(T value) {
        this.value = value;
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
    public PutEntityBaseRequest<T> withMutationId(String mutationId) {
        this.mutationId = mutationId;
        return this;
    }

    public PutEntityBaseRequest<T> withNamespaceEnabled(boolean namespaceEnabled) {
        this.namespaceEnabled = namespaceEnabled;
        return this;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public String getMutationId() {
        return mutationId;
    }

    public boolean getNamespaceEnabled() {
        return namespaceEnabled;
    }

}
