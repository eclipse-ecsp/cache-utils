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

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * IgniteCacheRedisImpl Sentinel Integration Test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RedisConfig.class })
@TestPropertySource("/ignite-cache-sentinel.properties")
@TestExecutionListeners(
        listeners = { ShutdownExecutionListener.class },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class IgniteCacheRedisImplSentinelIntegrationTest {

    @ClassRule
    public static EmbeddedRedisSentinelServer redis = new EmbeddedRedisSentinelServer();
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedisConfig() {
        Assert.assertNotNull(redissonClient);
    }
}
