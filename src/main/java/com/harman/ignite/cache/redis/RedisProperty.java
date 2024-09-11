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
 * Redis Properties.
 */
public abstract class RedisProperty {

    private RedisProperty() {
        throw new IllegalStateException("Utility class");
    }

    public static final String REDIS_ADDRESS = "redis.address";
    public static final String REDIS_SENTINELS = "redis.sentinels";
    public static final String REDIS_MASTER_NAME = "redis.master.name";
    public static final String REDIS_READ_MODE = "redis.read.mode";
    public static final String REDIS_SUBSCRIPTION_MODE = "redis.subscription.mode";
    public static final String REDIS_SUBSCRIPTION_CONN_MIN_IDLE_SIZE = "redis.subscription.conn.min.idle.size";
    public static final String REDIS_SUBSCRIPTION_CONN_POOL_SIZE = "redis.subscription.conn.pool.size";
    public static final String REDIS_SLAVE_CONN_MIN_IDLE_SIZE = "redis.slave.conn.min.idle.size";
    public static final String REDIS_SLAVE_POOL_SIZE = "redis.slave.pool.size";
    public static final String REDIS_MASTER_CONN_MIN_IDLE_SIZE = "redis.master.conn.min.idle.size";
    public static final String REDIS_MASTER_CONN_POOL_SIZE = "redis.master.conn.pool.size";
    public static final String REDIS_IDLE_CONN_TIMEOUT = "redis.idle.conn.timeout";
    public static final String REDIS_CONN_TIMEOUT = "redis.conn.timeout";
    public static final String REDIS_TIMEOUT = "redis.timeout";
    public static final String REDIS_RETRY_ATTEMPTS = "redis.retry.attempts";
    public static final String REDIS_RETRY_INTERVAL = "redis.retry.interval";
    public static final String REDIS_RECONNECTION_TIMEOUT = "redis.reconnection.timeout";
    public static final String REDIS_FAILED_ATTEMPTS = "redis.failed.attempts";
    public static final String REDIS_DATABASE = "redis.database";
    public static final String REDIS_PASSWORD = "redis.password";
    public static final String REDIS_SUBSCRIPTION_PER_CONN = "redis.subscriptions.per.conn";
    public static final String REDIS_CLIENT_NAME = "redis.client.name";
    public static final String REDIS_CONN_MIN_IDLE_SIZE = "redis.conn.min.idle.size";
    public static final String REDIS_CONN_POOL_SIZE = "redis.conn.pool.size";
    public static final String REDIS_CLUSTER_MASTERS = "redis.cluster.masters";
    public static final String REDIS_SCAN_INTERVAL = "redis.scan.interval";
    public static final String REDIS_NETTY_THREADS = "redis.netty.threads";
    public static final String REDIS_DECODE_IN_EXECUTOR = "redis.decode.in.executor";
    public static final String REDIS_EXECUTOR_THREADS = "redis.executor.threads";
    public static final String REDIS_KEEP_ALIVE = "redis.keep.alive";
    public static final String REDIS_PING_CONNECTION_INTERVAL = "redis.ping.connection.interval";
    public static final String REDIS_TCP_NO_DELAY = "redis.tcp.no.delay";
    public static final String REDIS_TRANSPORT_MODE = "redis.transport.mode";
    public static final String REDIS_HEALTH_MONITOR_ENABLED = "health.redis.monitor.enabled";
    public static final String REDIS_NEEDS_RESTART_ON_FAILURE = "health.redis.needs.restart.on.failure";
    public static final String REDIS_KEY_NAMESPACE_DELIMETER = ":";
    public static final String REDIS_CHECK_SLOTS_COVERAGE = "redis.check.slots.coverage";
}