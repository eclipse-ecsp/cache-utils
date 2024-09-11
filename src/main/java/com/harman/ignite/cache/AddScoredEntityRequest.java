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
 * Represents the options for adding an entity to a scored sorted set.
 *
 * @author ssasidharan
 * @param <T> Any type that implements IgniteEntity (enforced by IgniteCache contract)
 *
 */
public class AddScoredEntityRequest<T> {
    /**
     * Mandatory attribute. The key to associate with the value.
     */
    private String key;
    /**
     * Set entries will be ordered by this value. Mandatory.
     */
    private double score;
    /**
     * The actual value to associate with the score. Mandatory attribute.
     */
    private T value;
    /**
     * Optional attribute. The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     */
    private String mutationId;

    private boolean namespaceEnabled;

    public AddScoredEntityRequest() {
        this.namespaceEnabled = true;
    }

    public AddScoredEntityRequest<T> withKey(String key) {
        this.key = key;
        return this;
    }

    public AddScoredEntityRequest<T> withScore(double score) {
        this.score = score;
        return this;
    }

    public AddScoredEntityRequest<T> withValue(T value) {
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
    public AddScoredEntityRequest<T> withMutationId(String mutationId) {
        this.mutationId = mutationId;
        return this;
    }

    public AddScoredEntityRequest<T> withNamespaceEnabled(boolean namespaceEnabled) {
        this.namespaceEnabled = namespaceEnabled;
        return this;
    }

    public String getKey() {
        return key;
    }

    public double getScore() {
        return score;
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
