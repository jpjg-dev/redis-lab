package com.jipi.redis_lab.string.controller;

import com.jipi.redis_lab.string.dto.CounterResponse;
import com.jipi.redis_lab.string.service.RedisCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/counters/{name}")
@RequiredArgsConstructor
public class RedisCounterController {
    private final RedisCounterService redisCounterService;

    @PostMapping("/increment")
    public ResponseEntity<CounterResponse> increment(@PathVariable("name") String name, @RequestParam(value = "amount", defaultValue = "1") long amount) {
        long value = redisCounterService.increment(name, amount);
        CounterResponse response = new CounterResponse(name, value);
        return ResponseEntity.ok(response);
    }

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
