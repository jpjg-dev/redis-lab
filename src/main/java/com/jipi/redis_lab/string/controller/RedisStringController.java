package com.jipi.redis_lab.string.controller;

import com.jipi.redis_lab.string.dto.ExpiringStringValueRequest;
import com.jipi.redis_lab.string.dto.StringValueRequest;
import com.jipi.redis_lab.string.dto.StringValueResponse;
import com.jipi.redis_lab.string.service.RedisStringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

// 6~7강: Redis String 저장·조회와 TTL 포함 저장 요청을 처리하는 HTTP API
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/strings/{name}")
public class RedisStringController {
    private final RedisStringService redisStringService;

    // 6~7강: 일반 SET 요청이며 같은 Key를 덮어쓰면 기존 TTL이 제거된다.
    @PutMapping
    public ResponseEntity<Void> save(@PathVariable("name") String name, @RequestBody StringValueRequest stringValueRequest) {
        redisStringService.save(name, stringValueRequest.value());
        return ResponseEntity.noContent().build();
    }

    // 6강: Redis String 값을 조회하고 존재하지 않으면 404로 응답한다.
    @GetMapping
    public ResponseEntity<StringValueResponse> findByName(@PathVariable("name") String name) {
        return redisStringService.findbyname(name)
                .map(value -> new StringValueResponse(name, value))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 7강: 값과 TTL을 함께 저장해 SET 성공 후 EXPIRE가 누락되는 문제를 방지한다.
    @PutMapping("/expiration")
    public ResponseEntity<Void> expire(
            @PathVariable("name") String name,
            @RequestBody ExpiringStringValueRequest expiringStringValueRequest) {
        redisStringService.saveWithExpiration(
                name,
                expiringStringValueRequest.value(),
                Duration.ofSeconds(expiringStringValueRequest.ttlSeconds()));

        return ResponseEntity.noContent().build();
    }
}
