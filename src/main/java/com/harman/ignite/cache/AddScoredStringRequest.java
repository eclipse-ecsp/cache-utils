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
 * Represents options for adding a string to a scored sorted set.
 *
 * @author ssasidharan
 */
public class AddScoredStringRequest {
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
    private String value;
    /**
     * Optional attribute. The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     */
    private String mutationId;

    private boolean namespaceEnabled;

    public AddScoredStringRequest() {
        this.namespaceEnabled = true;
    }

    public AddScoredStringRequest withKey(String key) {
        this.key = key;
        return this;
    }

    public AddScoredStringRequest withScore(double score) {
        this.score = score;
        return this;
    }

    public AddScoredStringRequest withValue(String value) {
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
    public AddScoredStringRequest withMutationId(String mutationId) {
        this.mutationId = mutationId;
        return this;
    }

    public AddScoredStringRequest withNamespaceEnabled(boolean namespaceEnabled) {
        this.namespaceEnabled = namespaceEnabled;
        return this;
    }

    public String getKey() {
        return key;
    }

    public double getScore() {
        return score;
    }

    public String getValue() {
        return value;
    }

    public String getMutationId() {
        return mutationId;
    }

    public boolean getNamespaceEnabled() {
        return namespaceEnabled;
    }

}
