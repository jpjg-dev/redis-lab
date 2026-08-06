package com.jipi.redis_lab.list.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// 9강: Redis List를 이용한 Queue와 Stack 처리를 담당하는 서비스
@Service
@RequiredArgsConstructor
public class RedisListService {
    private final StringRedisTemplate stringRedisTemplate;


    // 4~9강: Queue와 Stack의 Key가 다른 자료구조와 충돌하지 않도록 Prefix를 분리한다.
    private static final String QUEUE_KEY_PREFIX = "queue:";
    private static final String STACK_KEY_PREFIX = "stack:";

    // 9강: RPUSH로 Queue의 오른쪽 끝에 새로운 데이터를 추가한다.
    public void enqueue(String name, String value) {
        listOperations().rightPush(createQueueKey(name), value);
    }

    // 9강: LPOP으로 Queue의 왼쪽에서 가장 오래된 데이터를 꺼낸다.
    public Optional<String> dequeue(String name) {
        String value =
                listOperations().leftPop(createQueueKey(name));

        return Optional.ofNullable(value);
    }

    // 9강: LRANGE로 Queue의 데이터를 오래된 순서부터 전체 조회한다.
    public Optional<List<String>> findQueueItems(String name) {
        return findItems(createQueueKey(name));
    }

    // 9강: LPUSH로 Stack의 왼쪽에 새로운 데이터를 추가한다.
    public void push(String name, String value) {
        listOperations().leftPush(createStackKey(name), value);
    }

    // 9강: LPOP으로 Stack에서 가장 최근에 추가된 데이터를 꺼낸다.
    public Optional<String> pop(String name) {
        String value =
                listOperations().leftPop(createStackKey(name));

        return Optional.ofNullable(value);
    }

    // 9강: LRANGE로 Stack의 데이터를 가장 최근 데이터부터 전체 조회한다.
    public Optional<List<String>> findStackItems(String name) {
        return findItems(createStackKey(name));
    }

    // 9강: 0부터 -1까지 조회하여 List 전체 데이터를 반환한다.
    private Optional<List<String>> findItems(String key) {
        List<String> items =
                listOperations().range(key, 0, -1);

        if (items == null || items.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(items);
    }

    private ListOperations<String, String> listOperations() {
        return stringRedisTemplate.opsForList();
    }

    private String createQueueKey(String name) {
        return QUEUE_KEY_PREFIX + name;
    }

    private String createStackKey(String name) {
        return STACK_KEY_PREFIX + name;
    }
}
