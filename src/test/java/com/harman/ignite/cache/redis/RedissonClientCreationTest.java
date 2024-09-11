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

import com.harman.ignite.cache.PutStringRequest;
import com.harman.ignite.utils.logger.IgniteLogger;
import com.harman.ignite.utils.logger.IgniteLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to test the creation of RedissonClient.
 * It also tests the cache operations with the created RedissonClient.
 */
public class RedissonClientCreationTest {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RedissonClientCreationTest.class);
    @ClassRule
    public static EmbeddedRedisServer embeddedRedisServer = new EmbeddedRedisServer();
    private RedissonClient redissonClient;
    private IgniteCacheRedisImpl igniteCacheRedisImpl;

    /**
     * This method is used to set up the test environment.
     */
    @Before
    public void setup() {

        LOGGER.info("Initializing redisson client...");
        Map<String, String> props = getPropertiesMap();
        RedisConfig redisConfig = new RedisConfig();
        igniteCacheRedisImpl = new IgniteCacheRedisImpl();
        redissonClient = redisConfig.builder().build(props);
        igniteCacheRedisImpl.setRedissonClient(redissonClient);
    }

    @After
    public void stop() {
        LOGGER.info("Shutting down Redisson Client...");
        redissonClient.shutdown();
    }

    /**
     * In this test making sure redisson client is formed properly.
     */
    @Test
    public void testRedissonClientCreation() {
        Assert.assertNotNull(redissonClient);
    }

    /**
     * In this test we are doing cache operations with the previously created redissonclient.
     */
    @Test
    public void testPutGetString() {
        Assert.assertNotNull(igniteCacheRedisImpl);
        igniteCacheRedisImpl.putString(new PutStringRequest().withKey("hello").withValue("world"));
        String value = igniteCacheRedisImpl.getString("hello");
        Assert.assertEquals("world", value);
    }

    private Map<String, String> getPropertiesMap() {
        Map<String, String> props = new HashMap<>();

        props.put("redis.address", "127.0.0.1:" + embeddedRedisServer.getMappedPort());
        props.put("redis.sentinels", "");
        props.put("redis.master.name", "");
        props.put("redis.dns.monitoring.interval", "5000");
        props.put("redis.read.mode", "SLAVE");
        props.put("redis.subscription.mode", "SLAVE");
        props.put("redis.subscription.conn.min.idle.size", "1");
        props.put("redis.subscription.conn.pool.size", "50");
        props.put("redis.slave.conn.min.idle.size", "32");
        props.put("redis.slave.pool.size", "64");
        props.put("redis.master.conn.min.idle.size", "32");
        props.put("redis.master.conn.pool.size", "64");
        props.put("redis.idle.conn.timeout", "10000");
        props.put("redis.conn.timeout", "10000");
        props.put("redis.timeout", "3000");
        props.put("redis.retry.attempts", "3");
        props.put("redis.retry.interval", "1500");
        props.put("redis.reconnection.timeout", "3000");
        props.put("redis.failed.attempts", "3");
        props.put("redis.database", "0");
        props.put("redis.password", "");
        props.put("redis.subscriptions.per.conn", "5");
        props.put("redis.client.name", "yellow");
        props.put("redis.conn.min.idle.size", "32");
        props.put("redis.conn.pool.size", "64");
        props.put("redis.cluster.masters", "");
        props.put("redis.scan.interval", "1000");
        props.put("redis.scan.limit", "10");
        props.put("redis.regex.scan.filename", "scanregex.txt");
        props.put("redis.netty.threads", "0");
        props.put("redis.decode.in.executor", "false");
        props.put("redis.check.slots.coverage", "false");
        props.put("redis.executor.threads", "32");
        props.put("redis.keep.alive", "true");
        props.put("redis.ping.connection.interval", "60000");
        props.put("redis.tcp.no.delay", "true");
        props.put("redis.transport.mode", "NIO");
        return props;
    }
}
