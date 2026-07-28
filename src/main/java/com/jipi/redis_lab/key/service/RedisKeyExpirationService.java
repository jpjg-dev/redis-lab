package com.jipi.redis_lab.key.service;

import com.jipi.redis_lab.key.dto.KeyExpirationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// 7강: Redis Key의 TTL 설정·조회·제거를 담당하는 서비스
@Service
@RequiredArgsConstructor
public class RedisKeyExpirationService {

    // 7강: TTL 조회 결과 -2는 Key 자체가 존재하지 않는 상태
    private static final long KEY_NOT_FOUND = -2L;

    // 7강: TTL 조회 결과 -1은 Key는 존재하지만 만료 시간이 없는 상태
    private static final long PERSISTENT_KEY = -1L;

    private final StringRedisTemplate stringRedisTemplate;

    // 7강: 기존 TTL에 시간을 더하지 않고 전달받은 TTL로 만료 시간을 덮어쓴다.
    public boolean expire(String key, Duration expiration) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, expiration));
    }

    // 7강: getExpire() 결과를 만료 중·영구 Key·존재하지 않는 Key로 구분한다.
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

    // 7강: Key는 삭제하지 않고 만료 시간만 제거해 영구 Key로 변경한다.
    public void persist(String key) {
        stringRedisTemplate.persist(key);
    }

}
