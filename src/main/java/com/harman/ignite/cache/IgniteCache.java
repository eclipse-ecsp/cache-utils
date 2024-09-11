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

package com.harman.ignite.cache;

import com.harman.ignite.entities.IgniteEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Base contract for cache in Ignite.
 * Support for optimized implementations that can perform batched updates through its .*Async() methods.
 * Clients of this interface are encouraged to use Async methods to improve throughput.
 * This may not be possible in all cases, for ex if the same value has to be read back immediately from Redis.
 * But for typical store and forget or store and retrieve slightly later use cases,
 * Async methods will bring about quite a bit of throughput improvement.
 * <br> <br>
 * Operations support 2 data types:
 * <li>String</li>
 * <li>IgniteEntity</li>
 *
 * @author ssasidharan
 */
public interface IgniteCache {
    String getString(String key);

    String getString(GetStringRequest request);

    void putString(PutStringRequest request);

    /**
     * Adds the put string mutation operation to a batch and completes the future when the batch is committed.
     *
     * @param request the put string request
     * @return future that returns the mutationId from the original request
     */
    Future<String> putStringAsync(PutStringRequest request);

    <T extends IgniteEntity> T getEntity(GetEntityRequest getRequest);

    <T extends IgniteEntity> T getEntity(String key);

    <T extends IgniteEntity> void putEntity(PutEntityRequest<T> putRequest);

    /**
     * Adds the put entity mutation operation to a batch and completes the future when the batch is committed.
     *
     * @param putRequest the put entity request
     * @return future that returns the mutationId from the original request
     */
    <T extends IgniteEntity> Future<String> putEntityAsync(PutEntityRequest<T> putRequest);

    void addStringToScoredSortedSet(AddScoredStringRequest request);

    /**
     * Adds the scored set string append mutation to a batch and completes the future when the batch is committed.
     *
     * @param request the add scored string request
     * @return future that returns the mutationId from the original request
     */
    Future<String> addStringToScoredSortedSetAsync(AddScoredStringRequest request);

    List<String> getStringsFromScoredSortedSet(GetScoredStringsRequest request);

    <T extends IgniteEntity> void addEntityToScoredSortedSet(AddScoredEntityRequest<T> request);

    /**
     * Adds the scored set entity append mutation to a batch and completes the future when the batch is committed.
     *
     * @param request the add scored entity request
     * @return future that returns the mutationId from the original request
     */
    <T extends IgniteEntity> Future<String> addEntityToScoredSortedSetAsync(AddScoredEntityRequest<T> request);

    <T extends IgniteEntity> List<T> getEntitiesFromScoredSortedSet(GetScoredEntitiesRequest request);

    <T extends IgniteEntity> Map<String, T> getKeyValuePairsForRegex(String keyRegex,
            Optional<Boolean> namespaceEnabled);

    void delete(String key);

    void delete(DeleteEntryRequest deleteRequest);

    Future<String> deleteAsync(DeleteEntryRequest deleteRequest);

    <T extends IgniteEntity> void putMapOfEntities(PutMapOfEntitiesRequest<T> request);

    <T extends IgniteEntity> Map<String, T> getMapOfEntities(GetMapOfEntitiesRequest request);

    void deleteMapOfEntities(DeleteMapOfEntitiesRequest request);

}
