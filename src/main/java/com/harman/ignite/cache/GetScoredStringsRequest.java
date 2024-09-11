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
 * Represents the options to get a range of string entries from a scored sorted set.
 *
 * @author ssasidharan
 */
public class GetScoredStringsRequest {
    /**
     * Mandatory attribute.
     */
    private String key;

    /**
     * Refer redis documentation.
     */
    private int startIndex;

    /**
     * Refer redis documentation.
     */
    private int endIndex;

    /**
     * False by default. Check redis documentation for details (for ex zrevrange)
     */
    private boolean reversed;

    private boolean namespaceEnabled;

    public GetScoredStringsRequest() {
        this.namespaceEnabled = true;
    }

    public GetScoredStringsRequest withKey(String key) {
        this.key = key;
        return this;
    }

    public GetScoredStringsRequest withStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public GetScoredStringsRequest withEndIndex(int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    public GetScoredStringsRequest fromReverseIndex() {
        this.reversed = true;
        return this;
    }

    public GetScoredStringsRequest withNamespaceEnabled(boolean namespaceEnabled) {
        this.namespaceEnabled = namespaceEnabled;
        return this;
    }

    public String getKey() {
        return key;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean getNamespaceEnabled() {
        return namespaceEnabled;
    }

}
