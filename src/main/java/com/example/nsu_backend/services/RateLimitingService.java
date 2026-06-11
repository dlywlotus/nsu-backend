package com.example.nsu_backend.services;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.github.bucket4j.Bucket;

@Component
public class RateLimitingService {
    private final LoadingCache<String, Bucket> loginBucketCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(key -> Bucket.builder()
                    .addLimit(limit -> limit.capacity(5)
                            .refillGreedy(5, Duration.ofMinutes(1))).build());

    private final LoadingCache<String, Bucket> createPostBucketCache = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.HOURS)
            .build(key -> Bucket.builder()
                    .addLimit(limit -> limit.capacity(10)
                            .refillGreedy(10, Duration.ofHours(1))).build());

    private final LoadingCache<String, Bucket> createCommentBucketCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(key -> Bucket.builder()
                    .addLimit(limit -> limit.capacity(60)
                            .refillGreedy(60, Duration.ofMinutes(1))).build());

    private final LoadingCache<String, Bucket> updateProfileIconBucketCache = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.HOURS)
            .build(key -> Bucket.builder()
                    .addLimit(limit -> limit.capacity(5)
                            .refillGreedy(5, Duration.ofHours(1))).build());

    public Bucket resolveLoginBucket(String key) {
        return loginBucketCache.get(key);
    }

    public Bucket resolveCreatePostBucket(String key) {
        return createPostBucketCache.get(key);
    }

    public Bucket resolveCreateCommentBucket(String key) {
        return createCommentBucketCache.get(key);
    }

    public Bucket resolveUpdateProfileIconBucket(String key) {
        return updateProfileIconBucketCache.get(key);
    }
}