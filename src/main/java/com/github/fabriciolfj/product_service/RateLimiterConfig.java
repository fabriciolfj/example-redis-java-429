package com.github.fabriciolfj.product_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Configuration
public class RateLimiterConfig{

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisScript<Long> redisRateLimiterScript;

    public boolean allowRequest(String key, int tokens, double refillRate, int capacity) {
        List<String> keys = Arrays.asList(
                key + ":tokens",
                key + ":timestamp"
        );

        Long result = redisTemplate.execute(
                redisRateLimiterScript,
                keys,
                String.valueOf(refillRate),
                String.valueOf(capacity),
                String.valueOf(Instant.now().getEpochSecond()),
                String.valueOf(tokens)
        );

        return result.intValue() == 1;
    }
}
