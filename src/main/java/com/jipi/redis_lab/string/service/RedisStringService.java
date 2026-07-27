package com.jipi.redis_lab.string.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisStringService {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String STRING_KEY_PREFIX = "string:";

    public void save(String name, String value) {
        stringRedisTemplate.opsForValue().set(createStringKey(name), value);
    }

    public Optional<String> findbyname(String name) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(createStringKey(name)));
    }

    private String createStringKey(String name) {
        return STRING_KEY_PREFIX + name;
    }
}
