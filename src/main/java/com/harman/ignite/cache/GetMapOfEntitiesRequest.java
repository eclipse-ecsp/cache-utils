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

import java.util.Set;

/**
 * Support for getting Map from IgniteCache.
 * Sample DataStructure - Map&lt;String,T&gt;
 *
 * @author avadakkootko
 */
public class GetMapOfEntitiesRequest extends GetEntityRequest {

    /**
     * This attribute is optional.
     * It is used to get specific key values from child map of multimap.
     * To get the entire map parent key is sufficient.
     */
    private Set<String> fields;

    public GetMapOfEntitiesRequest() {
        //default constructor
    }

    public GetMapOfEntitiesRequest withFields(Set<String> fields) {
        this.fields = fields;
        return this;
    }

    public Set<String> getFields() {
        return fields;
    }
}
