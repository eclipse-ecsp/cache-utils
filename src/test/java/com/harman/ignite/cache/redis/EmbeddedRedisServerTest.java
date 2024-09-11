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
import org.junit.Test;
import java.util.concurrent.TimeUnit;

import static com.harman.ignite.cache.redis.RedisConstants.TEN_THOUSAND;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

/**
 * Test case for the {@link EmbeddedRedisServer} class.
 */
public class EmbeddedRedisServerTest {

    @Test
    public void testAvailablePort() throws Throwable {
        EmbeddedRedisServer redis1 = new EmbeddedRedisServer();
        redis1.before();
        await().atMost(TEN_THOUSAND.getValue(), TimeUnit.MILLISECONDS);
        EmbeddedRedisServer redis2 = new EmbeddedRedisServer();
        redis2.before();
        Assert.assertNotEquals(redis1.getMappedPort(), redis2.getMappedPort());
        redis1.after();
        redis2.after();
    }
}
