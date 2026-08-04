package com.jipi.redis_lab.hash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

// 8강: Redis Hash의 field 저장·조회·삭제를 담당하는 서비스
@Service
@RequiredArgsConstructor
public class RedisHashService {
    private static final String HASH_KEY_PREFIX = "hash:";
    private final StringRedisTemplate stringRedisTemplate;

    // HSET key field value...
    public void saveAll(String name, Map<String, String> entries) {
        hashOperations().putAll(createHashKey(name), entries);
    }

    // HSET key field value
    public void saveField(String name, String field, String value) {
        hashOperations().put(createHashKey(name), field, value);
    }

    // HGETALL key
    public Optional<Map<String, String>> findAll(String name) {
        Map<String, String> entries =
                hashOperations().entries(createHashKey(name));

        if (entries.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(entries);
    }

    // HGET key field
    public Optional<String> findField(String name, String field) {
        String value =
                hashOperations().get(createHashKey(name), field);

        return Optional.ofNullable(value);
    }

    // HDEL key field
    public boolean deleteField(String name, String field) {
        Long deletedCount =
                hashOperations().delete(createHashKey(name), field);

        return deletedCount != null && deletedCount > 0;
    }

    private HashOperations<String, String, String> hashOperations() {
        return stringRedisTemplate.opsForHash();
    }

    private String createHashKey(String name) {
        return HASH_KEY_PREFIX + name;
    }

}
