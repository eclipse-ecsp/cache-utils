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
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer408;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;

/**
 * Embedded Redis Server.
 */
public class EmbeddedRedisServer extends ExternalResource {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(EmbeddedRedisServer.class);
    private RedisServer408 redis = null;
    private int port = 0;
    private int mappedPort = 0;

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
        igniteProvider.override(OS.WINDOWS, Architecture.x86, "redis-server.exe");
        igniteProvider.override(OS.WINDOWS, Architecture.x86_64, "redis-server.exe");

        port = new PortScanner().getAvailablePort(RedisConstants.SERVER_PORT.getValue());
        mappedPort = port;
        LOGGER.debug("Starting embedded Redis on port {}", port);
        redis = new RedisServer408(igniteProvider, port);
        redis.start();
        RedisConfig.overridingPort = port;

        /*
         * Docker container for running UT on local
         */

        /*
        port = new PortScanner().getAvailablePort(6379);
        LOGGER.debug("Starting embedded Redis on port {}", port);
        redis = new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(port);
        redis.start();
        mappedPort = redis.getMappedPort(port);
        RedisConfig.overridingPort = mappedPort;
        */
    }

    @Override
    protected void after() {
        LOGGER.info("Stopping REDIS container...");
        redis.stop();
    }

    public int getPort() {
        return port;
    }

    public int getMappedPort() {
        return mappedPort;
    }

}
