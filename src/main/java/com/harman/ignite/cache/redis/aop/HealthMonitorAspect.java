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

package com.harman.ignite.cache.redis.aop;

import com.harman.ignite.cache.redis.IgniteCacheRedisImpl;
import com.harman.ignite.utils.logger.IgniteLogger;
import com.harman.ignite.utils.logger.IgniteLoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.client.RedisException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * This class is used to monitor the health of IgniteCacheRedisImpl.
 */
@Aspect
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class HealthMonitorAspect {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(HealthMonitorAspect.class);

    @Pointcut(value = "execution(* com.harman.ignite.cache.redis.IgniteCacheRedisImpl.*(..))")
    private void redisHealthMonitor() {
        //pointcut
    }

    /**
     * This method is used to set the healthy flag to false in case of any
     * exception in IgniteCacheRedisImpl.
     *
     * @param jp        JoinPoint
     * @param exception Throwable
     * @throws Throwable Raised exception
     */
    @AfterThrowing(value = "redisHealthMonitor()", throwing = "exception")
    public void afterThrowingAdvice(JoinPoint jp, Throwable exception)  {
        Object target = jp.getTarget();
        LOGGER.error("Exception occurred in IgniteCacheRedisImpl due to {}.", exception.toString());
        // Checking for only RedisException subclasses for which
        // IgniteCacheRedisImpl
        // healthy will be set to false
        boolean assignableFrom = RedisException.class.isAssignableFrom(exception.getClass());
        if (target instanceof IgniteCacheRedisImpl cacheRedisImpl && assignableFrom) {
            cacheRedisImpl.setHealthy(false);
        }
    }
}
