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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.harman.ignite.cache.exception.JacksonCodecException;
import com.harman.ignite.utils.logger.IgniteLogger;
import com.harman.ignite.utils.logger.IgniteLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.redisson.config.TransportMode;
import org.redisson.connection.balancer.RoundRobinLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

/**
 * This class is used to configure the Redisson client.
 * It is used to create a new RedissonClient object from the given RedisConfig object.
 *
 * @see RedissonClient
 * @see RedisConfig
 */
@Configuration
@ComponentScan(basePackages = { "com.harman.ignite" })
public class RedisConfig {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RedisConfig.class);
    // support integration tests
    @Value("${" + RedisProperty.REDIS_ADDRESS + "}")
    private String address = "localhost:6379";
    @Value("${" + RedisProperty.REDIS_SENTINELS + "}")
    private String sentinels = null;
    @Value("${" + RedisProperty.REDIS_MASTER_NAME + "}")
    private String masterName;
    @Value("${" + RedisProperty.REDIS_READ_MODE + "}")
    private String readMode;
    @Value("${" + RedisProperty.REDIS_SUBSCRIPTION_MODE + "}")
    private String subscriptionMode;
    @Value("${" + RedisProperty.REDIS_SUBSCRIPTION_CONN_MIN_IDLE_SIZE + "}")
    private Integer subscriptionConnectionMinimumIdleSize;
    @Value("${" + RedisProperty.REDIS_SUBSCRIPTION_CONN_POOL_SIZE + "}")
    private Integer subscriptionConnectionPoolSize;
    @Value("${" + RedisProperty.REDIS_SLAVE_CONN_MIN_IDLE_SIZE + "}")
    private Integer slaveConnectionMinimumIdleSize;
    @Value("${" + RedisProperty.REDIS_SLAVE_POOL_SIZE + "}")
    private Integer slaveConnectionPoolSize;
    @Value("${" + RedisProperty.REDIS_MASTER_CONN_MIN_IDLE_SIZE + "}")
    private Integer masterConnectionMinimumIdleSize;
    @Value("${" + RedisProperty.REDIS_MASTER_CONN_POOL_SIZE + "}")
    private Integer masterConnectionPoolSize;
    @Value("${" + RedisProperty.REDIS_IDLE_CONN_TIMEOUT + "}")
    private Integer idleConnectionTimeout;
    @Value("${" + RedisProperty.REDIS_CONN_TIMEOUT + "}")
    private Integer connectTimeout;
    @Value("${" + RedisProperty.REDIS_TIMEOUT + "}")
    private Integer timeout;
    @Value("${" + RedisProperty.REDIS_RETRY_ATTEMPTS + "}")
    private Integer retryAttempts;
    @Value("${" + RedisProperty.REDIS_RETRY_INTERVAL + "}")
    private Integer retryInterval;
    @Value("${" + RedisProperty.REDIS_RECONNECTION_TIMEOUT + "}")
    private Integer reconnectionTimeout;
    @Value("${" + RedisProperty.REDIS_FAILED_ATTEMPTS + "}")
    private Integer failedAttempts;
    @Value("${" + RedisProperty.REDIS_DATABASE + "}")
    private Integer database;
    @Value("${" + RedisProperty.REDIS_PASSWORD + "}")
    private String password;
    @Value("${" + RedisProperty.REDIS_SUBSCRIPTION_PER_CONN + "}")
    private Integer subscriptionsPerConnection;
    @Value("${" + RedisProperty.REDIS_CLIENT_NAME + "}")
    private String clientName;
    @Value("${" + RedisProperty.REDIS_CONN_MIN_IDLE_SIZE + "}")
    private Integer connectionMinimumIdleSize;
    @Value("${" + RedisProperty.REDIS_CONN_POOL_SIZE + "}")
    private Integer connectionPoolSize;
    @Value("${" + RedisProperty.REDIS_CLUSTER_MASTERS + "}")
    private String clusterMasters = null;
    @Value("${" + RedisProperty.REDIS_SCAN_INTERVAL + "}")
    private Integer scanInterval;
    @Value("${" + RedisProperty.REDIS_NETTY_THREADS + "}")
    private Integer nettyThreads;
    @Value("${" + RedisProperty.REDIS_DECODE_IN_EXECUTOR + "}")
    private boolean decodeInExecutor;
    @Value("${" + RedisProperty.REDIS_EXECUTOR_THREADS + "}")
    private int threads;
    @Value("${" + RedisProperty.REDIS_KEEP_ALIVE + "}")
    private boolean keepAlive;
    @Value("${" + RedisProperty.REDIS_PING_CONNECTION_INTERVAL + "}")
    private int pingConnectionInterval;
    @Value("${" + RedisProperty.REDIS_TCP_NO_DELAY + "}")
    private boolean tcpNoDelay;
    @Value("${" + RedisProperty.REDIS_TRANSPORT_MODE + "}")
    private TransportMode transportMode;
    // RTC-156940 - Loading custom Ignite Json Jackson codec specific to DMF /
    // ADA flow to encode and decode the RetryRecords without using @class
    // parameter.
    @Value("${ignite.codec.class:}")
    private String igniteCodecClass;
    /*
     * RTC 326560. Encountered an issue where this decoder is also decoding RetryRecordId type of data
     * which caused ClassCastException in DMA RetryHandler while processing retries.
     * Since this decoder was decoding RetryRecordId type of data and converting the same
     * into RetryRecord type, hence DMA was storing RetryRecord where RetryRecordId is expected.
     * And at the time of get from in-memory, ClassCastException was being thrown as RetryRecordId
     * type is expected but DMA has RetryRecord type.
     *
     * hence below pattern is used to identify that value to decode is intended for RetryRecordIdDecoder.
     */
    @Value("${retry.record.id.pattern}")
    private String retryRecordIdPattern;
    
    @Value("${" + RedisProperty.REDIS_CHECK_SLOTS_COVERAGE + ":false}")
    private boolean checkSlotsCoverage;

    @Autowired
    private ObjectMapper objectMapper;

    // support integration tests
    static Integer overridingPort;
    static Integer[] overridingSentinelPorts;
    public static final String REDIS_ADDRESS_PREFIX = "redis://";

    @Bean
    public RedissonClient redissonClient() {
        return Redisson.create(getConfig());
    }

    private void setRedisConfig(Map<String, String> props) {

        address = props.get(RedisProperty.REDIS_ADDRESS);
        sentinels = props.get(RedisProperty.REDIS_SENTINELS);
        masterName = props.get(RedisProperty.REDIS_MASTER_NAME);
        readMode = props.get(RedisProperty.REDIS_READ_MODE);
        subscriptionMode = props.get(RedisProperty.REDIS_SUBSCRIPTION_MODE);
        subscriptionConnectionMinimumIdleSize =
                Integer.parseInt(props.get(RedisProperty.REDIS_SUBSCRIPTION_CONN_MIN_IDLE_SIZE));
        subscriptionConnectionPoolSize = Integer.parseInt(props.get(RedisProperty.REDIS_SUBSCRIPTION_CONN_POOL_SIZE));
        slaveConnectionMinimumIdleSize = Integer.parseInt(props.get(RedisProperty.REDIS_SLAVE_CONN_MIN_IDLE_SIZE));
        slaveConnectionPoolSize = Integer.parseInt(props.get(RedisProperty.REDIS_SLAVE_POOL_SIZE));
        masterConnectionMinimumIdleSize = Integer.parseInt(props.get(RedisProperty.REDIS_MASTER_CONN_MIN_IDLE_SIZE));
        masterConnectionPoolSize = Integer.parseInt(props.get(RedisProperty.REDIS_MASTER_CONN_POOL_SIZE));
        idleConnectionTimeout = Integer.parseInt(props.get(RedisProperty.REDIS_IDLE_CONN_TIMEOUT));
        connectTimeout = Integer.parseInt(props.get(RedisProperty.REDIS_CONN_TIMEOUT));
        timeout = Integer.parseInt(props.get(RedisProperty.REDIS_TIMEOUT));
        retryAttempts = Integer.parseInt(props.get(RedisProperty.REDIS_RETRY_ATTEMPTS));
        retryInterval = Integer.parseInt(props.get(RedisProperty.REDIS_RETRY_INTERVAL));
        reconnectionTimeout = Integer.parseInt(props.get(RedisProperty.REDIS_RECONNECTION_TIMEOUT));
        failedAttempts = Integer.parseInt(props.get(RedisProperty.REDIS_FAILED_ATTEMPTS));
        database = Integer.parseInt(props.get(RedisProperty.REDIS_DATABASE));
        password = props.get(RedisProperty.REDIS_PASSWORD);
        subscriptionsPerConnection = Integer.parseInt(props.get(RedisProperty.REDIS_SUBSCRIPTION_PER_CONN));
        clientName = props.get(RedisProperty.REDIS_CLIENT_NAME);
        connectionMinimumIdleSize = Integer.parseInt(props.get(RedisProperty.REDIS_CONN_MIN_IDLE_SIZE));
        connectionPoolSize = Integer.parseInt(props.get(RedisProperty.REDIS_CONN_POOL_SIZE));
        clusterMasters = props.get(RedisProperty.REDIS_CLUSTER_MASTERS);
        scanInterval = Integer.parseInt(props.get(RedisProperty.REDIS_SCAN_INTERVAL));
        nettyThreads = Integer.parseInt(props.get(RedisProperty.REDIS_NETTY_THREADS));
        decodeInExecutor = Boolean.parseBoolean(props.get(RedisProperty.REDIS_DECODE_IN_EXECUTOR));
        threads = Integer.parseInt(props.get(RedisProperty.REDIS_EXECUTOR_THREADS));
        keepAlive = Boolean.parseBoolean(props.get(RedisProperty.REDIS_KEEP_ALIVE));
        pingConnectionInterval = Integer.parseInt(props.get(RedisProperty.REDIS_PING_CONNECTION_INTERVAL));
        tcpNoDelay = Boolean.parseBoolean(props.get(RedisProperty.REDIS_TCP_NO_DELAY));
        checkSlotsCoverage = Boolean.parseBoolean(props.get(RedisProperty.REDIS_CHECK_SLOTS_COVERAGE));
        String tsMode = props.get(RedisProperty.REDIS_TRANSPORT_MODE);
        if (StringUtils.isEmpty(tsMode)) {
            throw new IllegalArgumentException("redis.transport.mode cannot be null or empty");
        } else {
            transportMode = TransportMode.valueOf(tsMode);
        }
    }

    private Config getConfig() {
        Config config;
        Codec codec = getCodec();
        if ((sentinels != null) && (sentinels.trim().length() > 0)) {
            config = getConfigIfSentinelsPresent(codec);
        } else if ((clusterMasters != null) && (clusterMasters.trim().length() > 0)) {
            config = getConfigIfClusterMasterPresent(codec);
        } else {
            config = getDefaultConfig(codec);
        }
        return config;
    }

    private Codec getCodec() {
        Codec codec;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
        if (StringUtils.isBlank(igniteCodecClass)) {
            codec = getDefaultCodec(mapper);
        } else {
            codec = getCodecForCodecClass();
        }
        return codec;
    }

    private Codec getDefaultCodec(ObjectMapper mapper) {
        LOGGER.info("Loading default JsonJacksonCodec class....");
        return new JsonJacksonCodec(mapper);
    }

    private Codec getCodecForCodecClass() {
        try {
            // RTC-156940 - Redis issue when the component is not able
            // to send to device and we restart the component.
            // In order to fix the above issue, a custom codec is needed to
            // be loaded for DMF/ADA flow which will be used to encode and
            // decode the data to the Redis without including @class
            // parameter.
            LOGGER.info("Loading ignite codec class ....");
            Class<Codec> clazz = (Class<Codec>) Class.forName(igniteCodecClass);
            return clazz.getConstructor(ObjectMapper.class, String.class)
                    .newInstance(objectMapper, retryRecordIdPattern);
        } catch (InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException
                | ClassNotFoundException e) {
            LOGGER.error("Unable to load ignite json jackson codec : {}", igniteCodecClass);
            throw new JacksonCodecException(
                    String.format("Unable to load ignite json jackson codec : %s", igniteCodecClass),
                    e);
        }
    }

    private Config getConfigIfSentinelsPresent(Codec codec) {
        Config config = new Config();
        config.setThreads(threads)
                .setNettyThreads(nettyThreads)
                .setCodec(codec)
                .setTransportMode(transportMode)
                .useSentinelServers()
                .setMasterName(masterName)
                .addSentinelAddress(Arrays.stream(sentinels.split(","))
                        .map(s ->  REDIS_ADDRESS_PREFIX + s).toArray(String[]::new))
                .setReadMode(ReadMode.valueOf(readMode))
                .setSubscriptionMode(SubscriptionMode.valueOf(subscriptionMode))
                .setSubscriptionConnectionMinimumIdleSize(subscriptionConnectionMinimumIdleSize)
                .setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize)
                .setSubscriptionsPerConnection(subscriptionsPerConnection)
                .setSlaveConnectionMinimumIdleSize(slaveConnectionMinimumIdleSize)
                .setSlaveConnectionPoolSize(slaveConnectionPoolSize)
                .setMasterConnectionMinimumIdleSize(masterConnectionMinimumIdleSize)
                .setMasterConnectionPoolSize(masterConnectionPoolSize)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setConnectTimeout(connectTimeout)
                .setTimeout(timeout)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval)
                .setFailedSlaveReconnectionInterval(reconnectionTimeout)
                .setFailedSlaveCheckInterval(failedAttempts)
                .setDatabase(database)
                .setClientName(clientName)
                .setKeepAlive(keepAlive)
                .setPingConnectionInterval(pingConnectionInterval)
                .setTcpNoDelay(tcpNoDelay);

        if ((password != null) && (password.trim().length() > 0)) {
            config.useSentinelServers().setPassword(password);
        }
        return config;
    }

    private Config getConfigIfClusterMasterPresent(Codec codec) {
        Config config = new Config();
        config.setThreads(threads)
                .setNettyThreads(nettyThreads)
                .setCodec(codec)
                .setTransportMode(transportMode)
                .useClusterServers()
                .addNodeAddress(Arrays.stream(clusterMasters.split(","))
                        .map(s ->  REDIS_ADDRESS_PREFIX + s).toArray(String[]::new))
                .setScanInterval(scanInterval)
                .setLoadBalancer(new RoundRobinLoadBalancer())
                .setSubscriptionConnectionMinimumIdleSize(subscriptionConnectionMinimumIdleSize)
                .setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize)
                .setSubscriptionsPerConnection(subscriptionsPerConnection)
                .setConnectTimeout(connectTimeout)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setTimeout(timeout)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval)
                .setFailedSlaveReconnectionInterval(reconnectionTimeout)
                .setFailedSlaveCheckInterval(failedAttempts)
                .setClientName(clientName)
                .setKeepAlive(keepAlive)
                .setPingConnectionInterval(pingConnectionInterval)
                .setTcpNoDelay(tcpNoDelay)
                .setSlaveConnectionMinimumIdleSize(slaveConnectionMinimumIdleSize)
                .setSlaveConnectionPoolSize(slaveConnectionPoolSize)
                .setMasterConnectionMinimumIdleSize(masterConnectionMinimumIdleSize)
                .setMasterConnectionPoolSize(masterConnectionPoolSize)
                .setCheckSlotsCoverage(checkSlotsCoverage);
        if ((password != null) && (password.trim().length() > 0)) {
            config.useClusterServers().setPassword(password);
        }
        return config;
    }

    private Config getDefaultConfig(Codec codec) {
        Config config = new Config();
        String addressToUse = (overridingPort != null) ? (address.split(":")[0] + ":" + overridingPort) : address;
        config.setThreads(threads)
                .setNettyThreads(nettyThreads)
                .setCodec(codec)
                .setTransportMode(transportMode)
                .useSingleServer()
                .setAddress(REDIS_ADDRESS_PREFIX + addressToUse)
                .setSubscriptionConnectionMinimumIdleSize(subscriptionConnectionMinimumIdleSize)
                .setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize)
                .setSubscriptionsPerConnection(subscriptionsPerConnection)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectTimeout(connectTimeout)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setConnectTimeout(connectTimeout)
                .setTimeout(timeout)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval)
                .setDatabase(database)
                .setClientName(clientName)
                .setKeepAlive(keepAlive)
                .setPingConnectionInterval(pingConnectionInterval)
                .setTcpNoDelay(tcpNoDelay);
        config.setNettyThreads(nettyThreads);
        if ((password != null) && (password.trim().length() > 0)) {
            config.useSingleServer().setPassword(password);
        }
        return config;
    }

    public RedissonClientBuilder builder() {
        return new RedissonClientBuilder();
    }

    /** Builder class for RedissonClient. */
    public class RedissonClientBuilder {
        public RedissonClient build(Map<String, String> props) {
            setRedisConfig(props);
            return redissonClient();
        }

        private void setRedisConfig(Map<String, String> props) {

            address = props.get(RedisProperty.REDIS_ADDRESS);
            sentinels = props.get(RedisProperty.REDIS_SENTINELS);
            masterName = props.get(RedisProperty.REDIS_MASTER_NAME);
            readMode = props.get(RedisProperty.REDIS_READ_MODE);
            subscriptionMode = props.get(RedisProperty.REDIS_SUBSCRIPTION_MODE);
            subscriptionConnectionMinimumIdleSize = 
                    Integer.parseInt(props.get(RedisProperty.REDIS_SUBSCRIPTION_CONN_MIN_IDLE_SIZE));
            subscriptionConnectionPoolSize =
                    Integer.parseInt(props.get(RedisProperty.REDIS_SUBSCRIPTION_CONN_POOL_SIZE));
            slaveConnectionMinimumIdleSize =
                    Integer.parseInt(props.get(RedisProperty.REDIS_SLAVE_CONN_MIN_IDLE_SIZE));
            slaveConnectionPoolSize =
                    Integer.parseInt(props.get(RedisProperty.REDIS_SLAVE_POOL_SIZE));
            masterConnectionMinimumIdleSize =
                    Integer.parseInt(props.get(RedisProperty.REDIS_MASTER_CONN_MIN_IDLE_SIZE));
            masterConnectionPoolSize = Integer.parseInt(props.get(RedisProperty.REDIS_MASTER_CONN_POOL_SIZE));
            idleConnectionTimeout = Integer.parseInt(props.get(RedisProperty.REDIS_IDLE_CONN_TIMEOUT));
            connectTimeout = Integer.parseInt(props.get(RedisProperty.REDIS_CONN_TIMEOUT));
            timeout = Integer.parseInt(props.get(RedisProperty.REDIS_TIMEOUT));
            retryAttempts = Integer.parseInt(props.get(RedisProperty.REDIS_RETRY_ATTEMPTS));
            retryInterval = Integer.parseInt(props.get(RedisProperty.REDIS_RETRY_INTERVAL));
            reconnectionTimeout = Integer.parseInt(props.get(RedisProperty.REDIS_RECONNECTION_TIMEOUT));
            failedAttempts = Integer.parseInt(props.get(RedisProperty.REDIS_FAILED_ATTEMPTS));
            database = Integer.parseInt(props.get(RedisProperty.REDIS_DATABASE));
            password = props.get(RedisProperty.REDIS_PASSWORD);
            subscriptionsPerConnection = Integer.parseInt(props.get(RedisProperty.REDIS_SUBSCRIPTION_PER_CONN));
            clientName = props.get(RedisProperty.REDIS_CLIENT_NAME);
            connectionMinimumIdleSize = Integer.parseInt(props.get(RedisProperty.REDIS_CONN_MIN_IDLE_SIZE));
            connectionPoolSize = Integer.parseInt(props.get(RedisProperty.REDIS_CONN_POOL_SIZE));
            clusterMasters = props.get(RedisProperty.REDIS_CLUSTER_MASTERS);
            scanInterval = Integer.parseInt(props.get(RedisProperty.REDIS_SCAN_INTERVAL));
            nettyThreads = Integer.parseInt(props.get(RedisProperty.REDIS_NETTY_THREADS));
            decodeInExecutor = Boolean.parseBoolean(props.get(RedisProperty.REDIS_DECODE_IN_EXECUTOR));
            threads = Integer.parseInt(props.get(RedisProperty.REDIS_EXECUTOR_THREADS));
            keepAlive = Boolean.parseBoolean(props.get(RedisProperty.REDIS_KEEP_ALIVE));
            pingConnectionInterval = Integer.parseInt(props.get(RedisProperty.REDIS_PING_CONNECTION_INTERVAL));
            tcpNoDelay = Boolean.parseBoolean(props.get(RedisProperty.REDIS_TCP_NO_DELAY));
            checkSlotsCoverage = Boolean.parseBoolean(props.get(RedisProperty.REDIS_CHECK_SLOTS_COVERAGE));
            String tsMode = props.get(RedisProperty.REDIS_TRANSPORT_MODE);
            if (StringUtils.isEmpty(tsMode)) {
                throw new IllegalArgumentException("redis.transport.mode cannot be null or empty");
            } else {
                transportMode = TransportMode.valueOf(tsMode);
            }
        }
    }
}
