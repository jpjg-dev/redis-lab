package com.jipi.redis_lab.list.controller;

import com.jipi.redis_lab.list.dto.ListItemRequest;
import com.jipi.redis_lab.list.dto.ListItemResponse;
import com.jipi.redis_lab.list.dto.ListItemsResponse;
import com.jipi.redis_lab.list.service.RedisListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 9강: Redis List를 이용한 Queue와 Stack HTTP API
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lists")
public class RedisListController {
    private final RedisListService redisListService;

    // 9강: Queue의 마지막에 데이터를 추가하고 204로 응답한다.
    @PostMapping("/queues/{name}/items")
    public ResponseEntity<Void> enqueue(
            @PathVariable("name") String name,
            @RequestBody ListItemRequest request
    ) {
        redisListService.enqueue(name, request.value());

        return ResponseEntity.noContent().build();
    }

    // 9강: Queue에서 가장 오래된 데이터를 꺼내고, 비어 있으면 404로 응답한다.
    @DeleteMapping("/queues/{name}/items")
    public ResponseEntity<ListItemResponse> dequeue(
            @PathVariable("name") String name
    ) {
        return redisListService.dequeue(name)
                .map(value -> new ListItemResponse(name, value))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 9강: Queue 전체 데이터를 오래된 순서부터 조회한다.
    @GetMapping("/queues/{name}/items")
    public ResponseEntity<ListItemsResponse> findQueueItems(
            @PathVariable("name") String name
    ) {
        return redisListService.findQueueItems(name)
                .map(items ->
                        new ListItemsResponse(name, items, items.size()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 9강: Stack의 가장 위에 데이터를 추가하고 204로 응답한다.
    @PostMapping("/stacks/{name}/items")
    public ResponseEntity<Void> push(
            @PathVariable("name") String name,
            @RequestBody ListItemRequest request
    ) {
        redisListService.push(name, request.value());

        return ResponseEntity.noContent().build();
    }

    // 9강: Stack에서 가장 최근 데이터를 꺼내고, 비어 있으면 404로 응답한다.
    @DeleteMapping("/stacks/{name}/items")
    public ResponseEntity<ListItemResponse> pop(
            @PathVariable("name") String name
    ) {
        return redisListService.pop(name)
                .map(value -> new ListItemResponse(name, value))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 9강: Stack 전체 데이터를 가장 최근 데이터부터 조회한다.
    @GetMapping("/stacks/{name}/items")
    public ResponseEntity<ListItemsResponse> findStackItems(
            @PathVariable("name") String name
    ) {
        return redisListService.findStackItems(name)
                .map(items ->
                        new ListItemsResponse(name, items, items.size()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
