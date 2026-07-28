package com.jipi.redis_lab.string.controller;

import com.jipi.redis_lab.string.dto.ExpiringStringValueRequest;
import com.jipi.redis_lab.string.dto.StringValueRequest;
import com.jipi.redis_lab.string.dto.StringValueResponse;
import com.jipi.redis_lab.string.service.RedisStringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/strings/{name}")
public class RedisStringController {
    private final RedisStringService redisStringService;

    @PutMapping
    public ResponseEntity<Void> save(@PathVariable("name") String name, @RequestBody StringValueRequest stringValueRequest) {
        redisStringService.save(name, stringValueRequest.value());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<StringValueResponse> findByName(@PathVariable("name") String name) {
        return redisStringService.findbyname(name)
                .map(value -> new StringValueResponse(name, value))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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
