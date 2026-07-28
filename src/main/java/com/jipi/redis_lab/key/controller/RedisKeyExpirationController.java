package com.jipi.redis_lab.key.controller;

import com.jipi.redis_lab.key.dto.KeyExpirationResponse;
import com.jipi.redis_lab.key.service.RedisKeyExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

// 7강: Redis Key의 TTL 설정·조회·제거 요청을 처리하는 HTTP API
@RequestMapping("/api/v1/keys/{key}/expiration")
@RequiredArgsConstructor
@RestController
public class RedisKeyExpirationController {
    private final RedisKeyExpirationService redisKeyExpirationService;

    // 7강: 0 이하의 TTL을 차단하고 기존 Key의 만료 시간을 새 TTL로 설정한다.
    @PutMapping
    public ResponseEntity<Void> expire(
            @PathVariable("key") String key,
            @RequestParam("seconds") long seconds
    ) {
        if (seconds <= 0) {
            return ResponseEntity.badRequest()
                    .build();
        }

        boolean updated = redisKeyExpirationService.expire(
                key,
                Duration.ofSeconds(seconds)
        );

        if (!updated) {
            return ResponseEntity.notFound()
                    .build();
        }

        return ResponseEntity.noContent()
                .build();
    }

    // 7강: TTL이 -2인 존재하지 않는 Key는 404로 응답한다.
    @GetMapping
    public ResponseEntity<KeyExpirationResponse> findExpiration(
            @PathVariable("key") String key
    ) {
        return redisKeyExpirationService.findExpiration(key)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound()
                        .build());
    }

    // 7강: PERSIST를 사용해 Key는 유지하고 TTL만 제거한다.
    @DeleteMapping
    public ResponseEntity<Void> persist(
            @PathVariable("key") String key
    ) {
        redisKeyExpirationService.persist(key);

        return ResponseEntity.noContent()
                .build();
    }
}
