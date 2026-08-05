package com.jipi.redis_lab.string.controller;

import com.jipi.redis_lab.string.dto.CounterResponse;
import com.jipi.redis_lab.string.service.RedisCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 6강: Redis 숫자 카운터 증가·조회 요청을 처리하는 HTTP API
@RestController
@RequestMapping("/api/v1/counters/{name}")
@RequiredArgsConstructor
public class RedisCounterController {
    private final RedisCounterService redisCounterService;

    // 6강: amount 기본값 1을 사용해 카운터를 증가시키고 변경된 값을 반환한다.
    @PostMapping("/increment")
    public ResponseEntity<CounterResponse> increment(@PathVariable("name") String name, @RequestParam(value = "amount", defaultValue = "1") long amount) {
        long value = redisCounterService.increment(name, amount);
        CounterResponse response = new CounterResponse(name, value);
        return ResponseEntity.ok(response);
    }

    // 6강: 카운터 값을 조회하고 존재하지 않는 Key는 404로 응답한다.
    @GetMapping
    public ResponseEntity<CounterResponse> findByName(@PathVariable("name") String name) {
        return redisCounterService.findByName(name)
                .map(value -> new CounterResponse(
                        name,
                        value
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound()
                        .build());
    }
}
