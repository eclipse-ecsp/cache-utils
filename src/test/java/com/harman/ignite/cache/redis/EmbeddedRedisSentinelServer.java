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
import org.junit.rules.ExternalResource;
import redis.embedded.RedisCluster;
import redis.embedded.RedisCluster408;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisSentinel;
import redis.embedded.RedisServer;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;

import java.util.Arrays;

/**
 * Embedded Redis Sentinel Server.
 */
public class EmbeddedRedisSentinelServer extends ExternalResource {
    private static IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(EmbeddedRedisSentinelServer.class);
    private RedisCluster408 redis = null;

    @Override
    protected void before() throws Throwable {
        RedisExecProvider igniteProvider = RedisExecProvider.defaultProvider();
        igniteProvider.override(OS.MAC_OS_X, Architecture.x86,
                "redis-server-4.0.8.app");
        igniteProvider.override(OS.MAC_OS_X, Architecture.x86_64,
                "redis-server-4.0.8.app");
        igniteProvider.override(OS.UNIX, Architecture.x86,
                "redis-server-4.0.8");
        igniteProvider.override(OS.UNIX, Architecture.x86_64,
                "redis-server-4.0.8");
        int[] sentinelPorts = new int[RedisConstants.THREE.getValue()];
        PortScanner portScanner = new PortScanner();
        sentinelPorts[0] = portScanner.getAvailablePort(RedisConstants.SENTINEL_PORT.getValue());
        sentinelPorts[1] = portScanner.getAvailablePort(sentinelPorts[0] + 1);
        sentinelPorts[RedisConstants.TWO.getValue()] = portScanner.getAvailablePort(sentinelPorts[1] + 1);
        int[] serverPorts = new int[RedisConstants.TWO.getValue()];
        serverPorts[0] = portScanner.getAvailablePort(RedisConstants.SERVER_PORT.getValue());
        serverPorts[1] = portScanner.getAvailablePort(serverPorts[0] + 1);
        LOGGER.info("Sentinel ports {}, {}, {}",
                sentinelPorts[0],
                sentinelPorts[1],
                sentinelPorts[RedisConstants.TWO.getValue()]);
        RedisCluster rc = RedisCluster.builder().withSentinelBuilder(
                RedisSentinel.builder()
                        .redisExecProvider(igniteProvider))
                .withServerBuilder(RedisServer.builder().redisExecProvider(igniteProvider))
                .sentinelPorts(
                        Arrays.asList(sentinelPorts[0],
                                sentinelPorts[1],
                                sentinelPorts[RedisConstants.TWO.getValue()]))
                // .serverPorts(Arrays.asList(serverPorts[0], serverPorts[1]))
                .replicationGroup("mogambo", 1).build();
        redis = new RedisCluster408(rc);
        RedisConfig.overridingSentinelPorts = new Integer[] {sentinelPorts[0],
                sentinelPorts[1],
                sentinelPorts[RedisConstants.TWO.getValue()]};
        redis.start();
    }

    @Override
    protected void after() {
        redis.stop();
    }

}
