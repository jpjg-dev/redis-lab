package com.jipi.redis_lab.hash.controller;

import com.jipi.redis_lab.hash.dto.HashEntriesRequest;
import com.jipi.redis_lab.hash.dto.HashEntriesResponse;
import com.jipi.redis_lab.hash.dto.HashFieldValueRequest;
import com.jipi.redis_lab.hash.dto.HashFieldValueResponse;
import com.jipi.redis_lab.hash.service.RedisHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 8강: Redis Hash의 field 저장·조회·삭제 요청을 처리하는 HTTP API
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hashes/{name}")
public class RedisHashController {
    private final RedisHashService redisHashService;

    @PutMapping
    public ResponseEntity<Void> saveAll(
            @PathVariable("name") String name,
            @RequestBody HashEntriesRequest request
    ) {
        redisHashService.saveAll(name, request.entries());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/fields/{field}")
    public ResponseEntity<Void> saveField(
            @PathVariable("name") String name,
            @PathVariable("field") String field,
            @RequestBody HashFieldValueRequest request
    ) {
        redisHashService.saveField(name, field, request.value());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<HashEntriesResponse> findAll(
            @PathVariable("name") String name
    ) {
        return redisHashService.findAll(name)
                .map(entries -> new HashEntriesResponse(name, entries))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/fields/{field}")
    public ResponseEntity<HashFieldValueResponse> findField(
            @PathVariable("name") String name,
            @PathVariable("field") String field
    ) {
        return redisHashService.findField(name, field)
                .map(value ->
                        new HashFieldValueResponse(name, field, value))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/fields/{field}")
    public ResponseEntity<Void> deleteField(
            @PathVariable("name") String name,
            @PathVariable("field") String field
    ) {
        boolean deleted =
                redisHashService.deleteField(name, field);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }


}
