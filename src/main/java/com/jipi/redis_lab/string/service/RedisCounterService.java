package com.jipi.redis_lab.string.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

// 6강: Redis String 숫자 카운터 증가·조회를 담당하는 서비스
@Service
@RequiredArgsConstructor
public class RedisCounterService {
    // 4~6강: 일반 String과 카운터 Key의 충돌을 방지하는 전용 Prefix
    private static final String KEY_PREFIX = "counter:";
    private final StringRedisTemplate stringRedisTemplate;

    // 6강: INCRBY에 대응하며 Key가 없으면 0에서 시작해 값을 원자적으로 증가시킨다.
    public long increment(String name, long amount) {
        Long result = stringRedisTemplate.opsForValue().increment(createStringKey(name), amount);
        if (result == null) {
            throw new IllegalStateException(
                    "Redis 카운터 증가 결과가 null입니다."
            );
        }
        return result;
    }

    // 6강: GET으로 카운터 값을 조회하고 존재하지 않으면 Optional.empty()를 반환한다.
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
