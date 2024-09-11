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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harman.ignite.cache.AddScoredEntityRequest;
import com.harman.ignite.cache.AddScoredStringRequest;
import com.harman.ignite.cache.DeleteEntryRequest;
import com.harman.ignite.cache.DeleteMapOfEntitiesRequest;
import com.harman.ignite.cache.GetEntityRequest;
import com.harman.ignite.cache.GetMapOfEntitiesRequest;
import com.harman.ignite.cache.GetScoredEntitiesRequest;
import com.harman.ignite.cache.GetScoredStringsRequest;
import com.harman.ignite.cache.GetStringRequest;
import com.harman.ignite.cache.IgniteCache;
import com.harman.ignite.cache.PutEntityRequest;
import com.harman.ignite.cache.PutMapOfEntitiesRequest;
import com.harman.ignite.cache.PutStringRequest;
import com.harman.ignite.cache.exception.DecodeException;
import com.harman.ignite.cache.exception.FileNotFoundException;
import com.harman.ignite.cache.exception.IgniteCacheException;
import com.harman.ignite.cache.exception.JacksonCodecException;
import com.harman.ignite.cache.exception.RedisBatchProcessingException;
import com.harman.ignite.entities.IgniteEntity;
import com.harman.ignite.healthcheck.HealthMonitor;
import com.harman.ignite.utils.logger.IgniteLogger;
import com.harman.ignite.utils.logger.IgniteLoggerFactory;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.harman.ignite.cache.redis.RedisConstants.TEN;
import static com.harman.ignite.cache.redis.RedisConstants.TWO;
import static com.harman.ignite.cache.redis.RedisProperty.REDIS_KEY_NAMESPACE_DELIMETER;


/**
 * Implementation of IgniteCache for Redis backend.<br>
 * Supports pipelined batch executions through its .*Async() methods.<br>
 * Clients of this class are encouraged to use Async methods to improve throughput.<br>
 * This may not be possible in all cases, for ex if the same value has to be read back immediately from Redis.<br>
 * But for typical store and forget or store and retrieve slightly later use cases,
 * Async methods will bring about quite a bit of throughput improvement.<br>
 * Operations support 2 data types:<br>
 * <li>String</li>
 * <li>IgniteEntity</li>
 *
 * @author ssasidharan
 */
@Repository
public class IgniteCacheRedisImpl implements IgniteCache, HealthMonitor {
    private static final int NUM_BATCH_RETRIES = 5;
    public static final long MINUS_ONE_LONG = -1L;
    public static final String REDIS_HEALTH_GUAGE = "REDIS_HEALTH_GUAGE";
    public static final String REDIS_HEALTH_MONITOR = "REDIS_HEALTH_MONITOR";
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(IgniteCacheRedisImpl.class);
    @Value("${redis.scan.limit:100}")
    private int scanLimit;
    @Value("${redis.regex.scan.filename:scanregex.txt}")
    private String regexScanFileName;
    private StringCodec stringCodec = new StringCodec();
    private Decoder<Object> decoder;
    @Autowired
    private RedissonClient redissonClient;
    // RTC-156940 - Loading custom Ignite Json Jackson codec specific to DMF /
    // ADA flow to encode and decode the RetryRecords without using @class
    // parameter.
    @Value("${ignite.codec.class:}")
    private String igniteCodecClass;
    @Value("${retry.record.id.pattern}")
    private String retryRecordIdPattern;
    @Autowired
    private ObjectMapper objectMapper;
    private String scanRegexScript;
    /**
     * Pipelining batch size. See redis pipelining for more details.
     */
    @Value("${redis.pipeline.size:1000}")
    private int batchSize = 1000;
    // One thread could be committing the batch when another thread could be
    // adding to the batch. To avoid this, using a volatile and assigning a new
    // batch so other threads that are about to execute a batch op, will use the
    // new batch. And existing threads that might be adding to the batch when
    // the batch is currently about to be committed are left to fail-retry
    // semantics. The only error would be that the batch may not always contain
    // batchSize entries; there can be more but not less. That is ok.
    private volatile RBatch currentBatch = null;
    /*
     * used for tracking the current size of the batch and to trigger execution
     * when size equals batch size
     */
    private AtomicInteger batchCount = new AtomicInteger(0);
    private AtomicLong lastBatchExecTimestamp = new AtomicLong(System.currentTimeMillis());

    public static final String MANDATORY_VALUE = "value is mandatory";
    public static final String MANDATORY_KEY = "key is mandatory";

    @Value("${" + RedisProperty.REDIS_HEALTH_MONITOR_ENABLED + ":false}")
    private boolean redisHealthMonitorEnabled;

    @Value("${" + RedisProperty.REDIS_NEEDS_RESTART_ON_FAILURE + ":false}")
    private boolean needsRestartOnFailure;

    @Value(value = "#{\"${redis.key.namespace:}\".trim()}")
    private String redisKeyNamespace;

    private volatile boolean healthy = true;

    public IgniteCacheRedisImpl() {
        //default constructor
    }

    @Override
    public String getString(String key) {
        key = addNamespace(key, true);
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public String getString(GetStringRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        return (String) redissonClient.getBucket(request.getKey()).get();
    }

    @Override
    public void putString(PutStringRequest putRequest) {
        validate(putRequest);
        putRequest.withKey(addNamespace(putRequest.getKey(), putRequest.getNamespaceEnabled()));
        RBucket<String> bucket = redissonClient.getBucket(putRequest.getKey());
        if (putRequest.getExpectedValue() == null) {
            if (putRequest.getTtlMs() == MINUS_ONE_LONG) {
                bucket.set(putRequest.getValue());
            } else {
                bucket.set(putRequest.getValue(), putRequest.getTtlMs(), TimeUnit.MILLISECONDS);
            }
        } else {
            bucket.compareAndSet(putRequest.getExpectedValue(), putRequest.getValue());
        }
    }

    @Override
    public <T extends IgniteEntity> T getEntity(String key) {
        key = addNamespace(key, true);
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public <T extends IgniteEntity> T getEntity(GetEntityRequest request) {
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        return (T) redissonClient.getBucket(request.getKey()).get();
    }

    @Override
    public <T extends IgniteEntity> void putEntity(PutEntityRequest<T> putRequest) {
        validate(putRequest);
        putRequest.withKey(addNamespace(putRequest.getKey(), putRequest.getNamespaceEnabled()));
        RBucket<T> bucket = redissonClient.getBucket(putRequest.getKey());
        if (putRequest.getExpectedValue() == null) {
            if (putRequest.getTtlMs() == MINUS_ONE_LONG) {
                bucket.set(putRequest.getValue());
            } else {
                bucket.set(putRequest.getValue(), putRequest.getTtlMs(), TimeUnit.MILLISECONDS);
            }
        } else {
            bucket.compareAndSet(putRequest.getExpectedValue(), putRequest.getValue());
        }
    }

    @Override
    public void addStringToScoredSortedSet(AddScoredStringRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        RScoredSortedSet<String> sset = redissonClient.getScoredSortedSet(request.getKey());
        sset.add(request.getScore(), request.getValue());
    }

    @Override
    public List<String> getStringsFromScoredSortedSet(GetScoredStringsRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        RScoredSortedSet<String> sset = redissonClient.getScoredSortedSet(request.getKey());
        if (request.isReversed()) {
            return sset.entryRangeReversed(request.getStartIndex(), request.getEndIndex()).stream()
                    .map(ScoredEntry::getValue).collect(Collectors.toList());
        } else {
            return sset.entryRange(request.getStartIndex(), request.getEndIndex())
                    .stream()
                    .map(ScoredEntry::getValue)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public <T extends IgniteEntity> void addEntityToScoredSortedSet(AddScoredEntityRequest<T> request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        RScoredSortedSet<T> sset = redissonClient.getScoredSortedSet(request.getKey());
        sset.add(request.getScore(), request.getValue());
    }

    @Override
    public <T extends IgniteEntity> List<T> getEntitiesFromScoredSortedSet(GetScoredEntitiesRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        RScoredSortedSet<T> sset = redissonClient.getScoredSortedSet(request.getKey());
        if (request.isReversed()) {
            return sset.entryRangeReversed(request.getStartIndex(), request.getEndIndex())
                    .stream()
                    .map(ScoredEntry::getValue)
                    .toList();
        } else {
            return sset.entryRange(request.getStartIndex(), request.getEndIndex())
                    .stream()
                    .map(ScoredEntry::getValue)
                    .toList();
        }
    }

    @Override
    public Future<String> putStringAsync(PutStringRequest putRequest) {
        validate(putRequest);
        putRequest.withKey(addNamespace(putRequest.getKey(), putRequest.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RBucketAsync<String> bucket = currentBatch.getBucket(putRequest.getKey());
            final String mutationId = putRequest.getMutationId();
            if (putRequest.getExpectedValue() == null) {
                if (putRequest.getTtlMs() == MINUS_ONE_LONG) {
                    bucket.setAsync(putRequest.getValue())
                            .thenAccept(s -> f.complete(mutationId));
                } else {
                    bucket.setAsync(putRequest.getValue(), putRequest.getTtlMs(), TimeUnit.MILLISECONDS)
                            .thenAccept(s -> f.complete(mutationId));
                }
            } else {
                bucket.compareAndSetAsync(putRequest.getExpectedValue(), putRequest.getValue())
                        .thenAccept(s -> completeFuture(s, f, mutationId));
            }
        });
        return f;
    }

    @Override
    public <T extends IgniteEntity> Future<String> putEntityAsync(PutEntityRequest<T> putRequest) {
        validate(putRequest);
        putRequest.withKey(addNamespace(putRequest.getKey(), putRequest.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RBucketAsync<T> bucket = currentBatch.getBucket(putRequest.getKey());
            final String mutationId = putRequest.getMutationId();
            if (putRequest.getExpectedValue() == null) {
                if (putRequest.getTtlMs() == MINUS_ONE_LONG) {
                    bucket.setAsync(putRequest.getValue())
                            .thenAccept(s -> f.complete(mutationId));
                } else {
                    bucket.setAsync(putRequest.getValue(), putRequest.getTtlMs(), TimeUnit.MILLISECONDS)
                            .thenAccept(s -> f.complete(mutationId));
                }
            } else {
                bucket.compareAndSetAsync(putRequest.getExpectedValue(), putRequest.getValue())
                        .thenAccept(s -> completeFuture(s, f, mutationId));
            }
        });
        return f;
    }

    @Override
    public Future<String> addStringToScoredSortedSetAsync(AddScoredStringRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RScoredSortedSetAsync<String> sset = currentBatch.getScoredSortedSet(request.getKey());
            final String mutationId = request.getMutationId();
            sset.addAsync(request.getScore(), request.getValue())
                    .thenAccept(s -> completeFuture(s, f, mutationId));
        });
        return f;
    }

    @Override
    public <T extends IgniteEntity> Future<String> addEntityToScoredSortedSetAsync(AddScoredEntityRequest<T> request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RScoredSortedSetAsync<T> sset = currentBatch.getScoredSortedSet(request.getKey());
            final String mutationId = request.getMutationId();
            RFuture<Boolean> rf = sset.addAsync(request.getScore(), request.getValue());
            rf.thenAccept(s -> completeFuture(s, f, mutationId));
        });
        return f;
    }

    @Override
    public void delete(String key) {
        key = addNamespace(key, true);
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.delete();
    }

    @Override
    public void delete(DeleteEntryRequest deleteRequest) {
        validate(deleteRequest);
        deleteRequest.withKey(addNamespace(deleteRequest.getKey(), deleteRequest.getNamespaceEnabled()));
        redissonClient.getBucket(deleteRequest.getKey()).delete();
    }

    @Override
    public Future<String> deleteAsync(DeleteEntryRequest deleteRequest) {
        validate(deleteRequest);
        deleteRequest.withKey(addNamespace(deleteRequest.getKey(), deleteRequest.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RBucketAsync<String> bucket = currentBatch.getBucket(deleteRequest.getKey());
            final String mutationId = deleteRequest.getMutationId();
            bucket.deleteAsync().thenAccept(s -> f.complete(mutationId));

        });
        return f;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * This methods tries to scan redis keys with the regex provided and returns key value pairs.
     */
    @Override
    public <T extends IgniteEntity> Map<String, T> getKeyValuePairsForRegex(
            String keyRegex, Optional<Boolean> namespaceEnabled) {
        Map<String, T> keyValuePairs = new HashMap<>();
        if ((namespaceEnabled.isPresent() && Boolean.TRUE.equals(namespaceEnabled.get()))
                || namespaceEnabled.isEmpty()) {
            keyRegex = addNamespace(keyRegex, true);
        }
        if (scanLimit < TEN.getValue()) {
            scanLimit = (int) TEN.getValue();
            LOGGER.warn("Scan limit for redis cache should be at least 10. Changing scan limit to 10");
        }
        LOGGER.debug("Scanning Redis with ScanLimit {} and keyRegex {}", scanLimit, keyRegex);
        long cursor = 0L;
        do {
            List<Object> matches = redissonClient.getScript(stringCodec).eval(RScript.Mode.READ_ONLY,
                    scanRegexScript,
                    RScript.ReturnType.MULTI, Collections.emptyList(), cursor, scanLimit, keyRegex);
            if (!matches.isEmpty()) {
                cursor = (long) matches.get(0);
                LOGGER.debug("Received cursor value {}", cursor);
            } else {
                cursor = 0L;
                LOGGER.error("No more result found for regex scan. Exiting !!!");
            }
            int size = matches.size();
            for (int index = 1; index < size; index = (int) (index + TWO.getValue())) {
                String key = String.valueOf(matches.get(index));
                String value = String.valueOf(matches.get(index + 1));
                T entity;
                try {
                    entity = (T) decoder.decode(Unpooled.wrappedBuffer(value.getBytes()), null);
                    LOGGER.debug("Decoded entity for key {} is {}", key, entity);
                    keyValuePairs.put(key, entity);
                } catch (IOException e) {
                    LOGGER.error("Unable to decode value {} from cache for key {}", value, key, e);
                    throw new DecodeException(
                            String.format("Unable to decode value %s from cache for key %s", value, key), e);
                }
            }
        } while (cursor > 0);
        LOGGER.debug("Key Value Pairs for regex {} of total size {} being returned are as follows {}",
                keyRegex,
                keyValuePairs.size(),
                keyValuePairs);
        return keyValuePairs;
    }

    @Override
    public <T extends IgniteEntity> void putMapOfEntities(PutMapOfEntitiesRequest<T> mapRequest) {
        validate(mapRequest);
        mapRequest.withKey(addNamespace(mapRequest.getKey(), mapRequest.getNamespaceEnabled()));
        String key = mapRequest.getKey();
        Map<String, T> value = mapRequest.getValue();

        RMap<String, T> rmap = redissonClient.getMap(key);
        rmap.putAll(value);
        LOGGER.debug("Put map {} to Redis for key {}", value, key);
    }

    @Override
    public <T extends IgniteEntity> Map<String, T> getMapOfEntities(GetMapOfEntitiesRequest mapRequest) {
        validate(mapRequest);
        mapRequest.withKey(addNamespace(mapRequest.getKey(), mapRequest.getNamespaceEnabled()));
        String key = mapRequest.getKey();

        RMap<String, T> rmap = redissonClient.getMap(key);
        Set<String> fields = mapRequest.getFields();
        if (fields != null && !fields.isEmpty()) {
            LOGGER.debug("Attempting to get key value pairs from Redis for subkeys {} with key {}", fields, key);
            return rmap.getAll(fields);
        } else {
            LOGGER.debug("Attempting to get all key value pairs from Redis with parent key {}", key);
            return rmap.readAllMap();
        }
    }

    @Override
    public void deleteMapOfEntities(DeleteMapOfEntitiesRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        String key = request.getKey();
        Set<String> fields = request.getFields();
        if (fields != null && !fields.isEmpty()) {
            LOGGER.debug("Attempting to remove key value pairs from Redis for subkeys {} with key {}", fields, key);
            redissonClient.getMap(key).fastRemove(fields.toArray());
        } else {
            LOGGER.debug("Attempting to remove all key value pairs from Redis with parent key {}", key);
            redissonClient.getMap(key).delete();
        }

    }

    private String addNamespace(String key, boolean namespaceEnabled) {
        if (StringUtils.isNotEmpty(redisKeyNamespace) && namespaceEnabled) {
            LOGGER.debug("Namespace enabled: {}, Namespace value for redis: {}, for key: {}",
                    namespaceEnabled, redisKeyNamespace, key);
            key = redisKeyNamespace + REDIS_KEY_NAMESPACE_DELIMETER + key;
            LOGGER.debug("Ignite cache key with namespace {}", key);
        }
        return key;
    }

    /**
     * Executes the batch operation consumer in a reliable way. <br>
     * If a thread was performing a batch operation and another thread performed RBatch.execute() at the same time,
     * then first thread will fail with IllegalStateException("Batch already has been executed").
     * This method performs a retry for such scenarios. Also advances the batch state.
     *
     * @param c the batch operation consumer
     * @throws RuntimeException
     *         if NUM_BATCH_RETRIES exhausted, and it still fails
     */
    private void performBatchOperation(Consumer<Void> c) {
        for (int i = 1; i <= NUM_BATCH_RETRIES; i++) {
            try {
                c.accept(null);
                break;
            } catch (IllegalStateException ise) {
                if (!ise.getMessage().contains("Batch already has been executed")) {
                    throw ise;
                }
                if (i == NUM_BATCH_RETRIES) {
                    throw new RedisBatchProcessingException(
                            "Batch operation failed despite trying " + NUM_BATCH_RETRIES + " times");
                }
            } catch (NullPointerException npe) {
                // This has been introduced because it was observed in
                // integration test case
                // IgniteCacheRedisImplIntegrationTest.testBatchConcurrentExecutionException()
                if (i == NUM_BATCH_RETRIES) {
                    LOGGER.warn("SetAsync invoked before a new batch was created", npe);
                    throw new RedisBatchProcessingException(
                            "Batch operation failed despite trying " + NUM_BATCH_RETRIES + " times");
                }
            }
        }
        advanceBatchState();
    }

    private void advanceBatchState() {
        int size = batchCount.incrementAndGet();
        if (size % batchSize == 0) {
            // before executing the batch we will keep a reference and then swap
            // the main reference to a new instance of batch so clients can
            // continue adding to batch
            RBatch existingBatch = currentBatch;
            // now assign new batch to the same reference so that other threads
            // see the new batch and not the old one (currentBatch is volatile)
            startBatch();
            LOGGER.debug("Executing batch asynchronously");
            existingBatch.executeAsync().thenAccept(r -> {
                LOGGER.debug("Executed batch asynchronously");
                LOGGER.trace("Responses of last batch operation: {}",
                        r.getResponses());
            });
        }
    }

    private void completeFuture(boolean success, CompletableFuture<String> f, final String mutationId) {
        if (success) {
            f.complete(mutationId);
        } else {
            f.completeExceptionally(new RedisBatchProcessingException("Redis batch update failed"));
        }
    }

    // added to support test cases
    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // added to support test cases
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void setRBatch(RBatch batch) {
        this.currentBatch = batch;
    }

    @PostConstruct
    private void postConstruct() {
        try {
            scanRegexScript = readFile(regexScanFileName);
            LOGGER.info("Scan Regex file contents : {}", scanRegexScript);
        } catch (IOException e) {
            throw new IgniteCacheException(String.format("Unable to read from file : %s", regexScanFileName), e);
        }

        if (StringUtils.isBlank(igniteCodecClass)) {
            LOGGER.info("Loading decoder from default JsonJacksonCodec class....");
            decoder = JsonJacksonCodec.INSTANCE.getValueDecoder();
        } else {
            try {
                // RTC-156940 - Redis issue when the component is not able
                // to send to device, and we restart the component.
                // In order to fix the above issue, a custom codec is needed to
                // be loaded for DMF/ADA flow which will be used to encode and
                // decode the data to the Redis without including @class
                // parameter.
                LOGGER.info("Loading decoder from ignite codec  ....");
                Class<Codec> clazz = (Class<Codec>) Class.forName(igniteCodecClass);

                decoder = clazz.getConstructor(ObjectMapper.class, String.class)
                        .newInstance(objectMapper, retryRecordIdPattern)
                        .getValueDecoder();
            } catch (InstantiationException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException
                    | NoSuchMethodException
                    | SecurityException
                    | ClassNotFoundException e) {
                LOGGER.error("Unable to load ignite json jackson codec : {}", igniteCodecClass);
                throw new JacksonCodecException(
                        String.format("Unable to load ignite json jackson codec : %s", igniteCodecClass), e);
            }
        }
        startBatch();
    }

    private void startBatch() {
        currentBatch = redissonClient.createBatch();
        boolean updated = batchCount.compareAndSet(batchSize, 0);
        if (updated) {
            lastBatchExecTimestamp.set(System.currentTimeMillis());
        }
    }

    private void validate(PutMapOfEntitiesRequest<?> request) {
        Objects.requireNonNull(
                request.getKey(), "Received null/empty key in put map request.Aborting the request.");
        Objects.requireNonNull(
                request.getValue(), "Received null/empty value in put map request.Aborting the request.");
    }

    private void validate(GetMapOfEntitiesRequest request) {
        Objects.requireNonNull(request.getKey(), "Received null/empty key in get map request.Aborting the request.");
    }

    private void validate(DeleteMapOfEntitiesRequest request) {
        Objects.requireNonNull(request.getKey(), "Received null/empty key in delete map request.Aborting the request.");
    }

    private void validate(DeleteEntryRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
    }

    private void validate(GetStringRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
    }

    private void validate(AddScoredStringRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
        Objects.requireNonNull(request.getValue(), MANDATORY_VALUE);
    }

    private void validate(GetScoredStringsRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
    }

    private void validate(AddScoredEntityRequest<?> request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
        Objects.requireNonNull(request.getValue(), MANDATORY_VALUE);
    }

    private void validate(GetScoredEntitiesRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
    }

    private void validate(PutStringRequest putRequest) {
        Objects.requireNonNull(putRequest.getKey(), MANDATORY_KEY);
        Objects.requireNonNull(putRequest.getValue(), MANDATORY_VALUE);
    }

    private void validate(PutEntityRequest<?> putRequest) {
        Objects.requireNonNull(putRequest.getKey(), MANDATORY_KEY);
        Objects.requireNonNull(putRequest.getValue(), MANDATORY_VALUE);
    }

    String readFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new FileNotFoundException(fileName + " not found !!!");
        }
        return IOUtils.toString(inputStream, Charset.defaultCharset());
    }

    public void setHealthy(boolean isHealthy) {
        this.healthy = isHealthy;
    }

    @Override
    public boolean isHealthy(boolean forceHealthCheck) {
        if (forceHealthCheck) {
            healthy = forceHealthCheck();
        }
        return healthy;
    }

    private boolean forceHealthCheck() {
        try {
            putString(new PutStringRequest().withKey("hello").withValue("world").withNamespaceEnabled(false));
            delete(new DeleteEntryRequest().withKey("hello").withNamespaceEnabled(false));
            healthy = true;
        } catch (Exception ex) {
            healthy = false;
            LOGGER.error("Error occured during Redis forceHealthCheck : {}", ex.getCause());
        }
        return healthy;
    }

    @Override
    public String monitorName() {
        return REDIS_HEALTH_MONITOR;
    }

    @Override
    public boolean needsRestartOnFailure() {
        return needsRestartOnFailure;
    }

    @Override
    public String metricName() {
        return REDIS_HEALTH_GUAGE;
    }

    @Override
    public boolean isEnabled() {
        return redisHealthMonitorEnabled;
    }

}
