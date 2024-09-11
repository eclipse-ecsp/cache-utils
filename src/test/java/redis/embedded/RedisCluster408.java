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

package redis.embedded;

import com.harman.ignite.utils.logger.IgniteLogger;
import com.harman.ignite.utils.logger.IgniteLoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a wrapper for RedisCluster class.
 * It is used to create a new RedisServer408 object from the given Redis object.
 *
 * @see RedisCluster
 * @see Redis
 * @see RedisServer408
 * @see RedisSentinel408
 */
public class RedisCluster408 extends RedisCluster {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RedisCluster408.class);
    private RedisCluster rc;

    public RedisCluster408(RedisCluster rc) {
        this(rc.sentinels(), rc.servers());
    }

    public RedisCluster408(List<Redis> sentinels, List<Redis> servers) {
        super(transformSentinels(sentinels), transformServers(servers));
    }

    private static List<Redis> transformServers(List<Redis> servers) {
        return servers.stream().map(s -> createNewRedisServer408(s)).collect(Collectors.toList());
    }

    private static RedisServer408 createNewRedisServer408(Redis s) {
        try {
            return new RedisServer408((RedisServer) s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Redis> transformSentinels(List<Redis> sentinels) {
        return sentinels.stream().map(s -> new RedisSentinel408((RedisSentinel) s)).collect(Collectors.toList());
    }
}
