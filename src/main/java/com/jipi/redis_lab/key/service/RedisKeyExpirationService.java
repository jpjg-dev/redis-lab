package com.jipi.redis_lab.key.service;

import com.jipi.redis_lab.key.dto.KeyExpirationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisKeyExpirationService {
    private static final long KEY_NOT_FOUND = -2L;
    private static final long PERSISTENT_KEY = -1L;

    private final StringRedisTemplate stringRedisTemplate;


    public boolean expire(String key, Duration expiration) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, expiration));
    }

    public Optional<KeyExpirationResponse> findExpiration(String key) {
        Long remainingSeconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);

        if (remainingSeconds == null) {
            throw new IllegalStateException("Redis TTL 조회 결과가 NULL 입니다.");
        }

        if (remainingSeconds == KEY_NOT_FOUND) {
            return Optional.empty();
        }

        if (remainingSeconds == PERSISTENT_KEY) {
            return Optional.of(KeyExpirationResponse.persistent(key));
        }

        return Optional.of(KeyExpirationResponse.expiring(key, remainingSeconds));
    }

    public void persist(String key) {
        stringRedisTemplate.persist(key);
    }

}
