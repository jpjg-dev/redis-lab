package com.jipi.redis_lab.string.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

// 6~7강: Redis String 저장·조회와 TTL이 포함된 임시 값 저장을 담당하는 서비스
@Service
@RequiredArgsConstructor
public class RedisStringService {
    private final StringRedisTemplate stringRedisTemplate;

    // 4~6강: Key 충돌을 방지하고 용도를 구분하기 위한 String 전용 Prefix
    private static final String STRING_KEY_PREFIX = "string:";

    // 6~7강: 일반 SET으로 기존 Key를 덮어쓰면 기존 TTL도 함께 제거된다.
    public void save(String name, String value) {
        stringRedisTemplate.opsForValue().set(createStringKey(name), value);
    }

    // 6강: GET 명령에 대응하며 존재하지 않는 값은 Optional.empty()로 반환한다.
    public Optional<String> findbyname(String name) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(createStringKey(name)));
    }

    // 7강: SET과 EXPIRE를 분리하지 않고 값과 TTL을 하나의 명령으로 함께 저장한다.
    public void saveWithExpiration(String name, String value, Duration expiration) {
        stringRedisTemplate.opsForValue().set(createStringKey(name), value, expiration);
    }

    private String createStringKey(String name) {
        return STRING_KEY_PREFIX + name;
    }
}
