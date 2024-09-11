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

package com.harman.ignite.cache.redis;

/**
 * Constants for Redis.
 */
public enum RedisConstants {
    MINUS_ONE(-1),
    TWO(2),
    THREE(3),
    FIVE(5),
    TEN(10),
    HUNDRED(100),
    ONE_FIFTY(150),
    TWO_HUNDRED(200),
    THOUSAND(1000),
    SERVER_PORT(6379),
    SENTINEL_PORT(26379),
    TEN_THOUSAND(10000);

    private final int value;

    RedisConstants(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
