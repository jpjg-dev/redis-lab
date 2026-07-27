package com.jipi.redis_lab.string.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisCounterService {
    private static final String KEY_PREFIX = "counter:";
    private final StringRedisTemplate stringRedisTemplate;

    public long increment(String name, long amount) {
        Long result = stringRedisTemplate.opsForValue().increment(createStringKey(name), amount);
        if (result == null) {
            throw new IllegalStateException(
                    "Redis 카운터 증가 결과가 null입니다."
            );
        }
        return result;
    }

    public Optional<Long> findByName(String name) {
        String value = stringRedisTemplate.opsForValue()
                .get(createStringKey(name));

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(Long.parseLong(value));
    }

    private String createStringKey(String name) {
        return KEY_PREFIX + name;
    }
}
