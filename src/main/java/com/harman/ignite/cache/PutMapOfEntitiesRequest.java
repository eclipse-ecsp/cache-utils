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

import java.util.Map;

/**
 * Support for putting a Map in to IgniteCache.
 * Sample DataStructure - Map&lt;String,T&gt;&gt; where T is any type that implements IgniteEntity
 *
 * @author avadakkootko
 */
public class PutMapOfEntitiesRequest<V> extends PutEntityBaseRequest<Map<String, V>> {

}
