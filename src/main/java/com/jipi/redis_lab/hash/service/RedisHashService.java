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
    // 4~8강: 다른 자료구조의 Key와 충돌하지 않도록 Hash 전용 Prefix를 적용한다.
    private static final String HASH_KEY_PREFIX = "hash:";
    private final StringRedisTemplate stringRedisTemplate;

    // 8강: HSET으로 여러 Field-Value를 하나의 Hash에 일괄 저장한다.
    public void saveAll(String name, Map<String, String> entries) {
        hashOperations().putAll(createHashKey(name), entries);
    }

    // 8강: HSET으로 특정 Field를 저장하며 같은 Field가 있으면 값을 덮어쓴다.
    public void saveField(String name, String field, String value) {
        hashOperations().put(createHashKey(name), field, value);
    }

    // 8강: HGETALL로 전체 Field를 조회하고 비어 있으면 Optional.empty()를 반환한다.
    public Optional<Map<String, String>> findAll(String name) {
        Map<String, String> entries =
                hashOperations().entries(createHashKey(name));

        if (entries.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(entries);
    }

    // 8강: HGET으로 특정 Field를 조회하고 존재하지 않으면 Optional.empty()를 반환한다.
    public Optional<String> findField(String name, String field) {
        String value =
                hashOperations().get(createHashKey(name), field);

        return Optional.ofNullable(value);
    }

    // 8강: HDEL로 특정 Field를 삭제하고 실제 삭제 여부를 반환한다.
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
