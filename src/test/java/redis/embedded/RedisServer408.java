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

import java.io.IOException;

/**
 * Supporting class for Redis version 4.0.8.
 *
 * @author ssasidharan
 */
public class RedisServer408 extends RedisServer {

    public RedisServer408(RedisExecProvider redisExecProvider, Integer port) throws IOException {
        super(redisExecProvider, port);
    }

    public RedisServer408(RedisServer r) throws IOException {
        super(r.ports().get(0));
        this.args = r.args;
    }

    @Override
    protected String redisReadyPattern() {
        return ".*Ready to accept connections.*";
    }

}
