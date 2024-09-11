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

import com.harman.ignite.utils.logger.IgniteLogger;
import com.harman.ignite.utils.logger.IgniteLoggerFactory;
import org.redisson.api.RedissonClient;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This class is used to shut down the redisson client after the test class execution.
 */
public class ShutdownExecutionListener extends AbstractTestExecutionListener {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ShutdownExecutionListener.class);

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        // initialize resources
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        LOGGER.info("Shutting down redisson client...");
        RedissonClient redissonClient = (RedissonClient) testContext.getApplicationContext().getBean("redissonClient");
        redissonClient.shutdown();
    }
}
