package com.taskflow.auth.infrastructure.adapter.out.redis;

import com.taskflow.auth.application.port.out.RefreshTokenStoragePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisRefreshTokenAdapter implements RefreshTokenStoragePort {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;

    public RedisRefreshTokenAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void store(String email, String token, long ttlMillis) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, token, ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public String get(String email) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + email);
    }

    @Override
    public void delete(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
