package com.jipi.redis_lab.key.controller;

import com.jipi.redis_lab.key.dto.KeyExpirationResponse;
import com.jipi.redis_lab.key.service.RedisKeyExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RequestMapping("/api/v1/keys/{key}/expiration")
@RequiredArgsConstructor
@RestController
public class RedisKeyExpirationController {
    private final RedisKeyExpirationService redisKeyExpirationService;

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

    @GetMapping
    public ResponseEntity<KeyExpirationResponse> findExpiration(
            @PathVariable("key") String key
    ) {
        return redisKeyExpirationService.findExpiration(key)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound()
                        .build());
    }

    @DeleteMapping
    public ResponseEntity<Void> persist(
            @PathVariable("key") String key
    ) {
        redisKeyExpirationService.persist(key);

        return ResponseEntity.noContent()
                .build();
    }
}
