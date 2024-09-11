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
 * Represents the options for getting a string from cache.
 *
 * @author ssasidharan
 */
public class GetStringRequest {
    /**
     * Mandatory attribute.
     */
    private String key;

    private boolean namespaceEnabled;

    public GetStringRequest() {
        this.namespaceEnabled = true;
    }

    public GetStringRequest withKey(String key) {
        this.key = key;
        return this;
    }

    public GetStringRequest withNamespaceEnabled(boolean namespaceEnabled) {
        this.namespaceEnabled = namespaceEnabled;
        return this;
    }

    public String getKey() {
        return key;
    }

    public boolean getNamespaceEnabled() {
        return namespaceEnabled;
    }

}
