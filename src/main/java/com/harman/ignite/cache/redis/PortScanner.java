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

import com.harman.ignite.cache.exception.IgniteCacheException;
import com.harman.ignite.utils.logger.IgniteLogger;
import com.harman.ignite.utils.logger.IgniteLoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

/**
 * This class is used to scan the available port.
 */
public class PortScanner {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(PortScanner.class);
    private static final int MAX_PORT =  65535;

    /**
     * This method is used to get the available port.
     *
     * @param port port
     * @return available port
     */    
    public int getAvailablePort(int port) {
        while (true) {
            if (port > MAX_PORT) {
                throw new IgniteCacheException("Max port limit has been reached while creating the socket");
            }
            try (Socket s = SSLSocketFactory.getDefault().createSocket("localhost", port)) {
                LOGGER.debug("Port {} is not available", port);
            } catch (IOException e) {
                return port;
            }
            port = port + 1;
        }
    }
}
