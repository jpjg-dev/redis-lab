package com.jipi.redis_lab.set.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

// 10강: Redis Set의 중복 제거와 집합 연산을 담당하는 서비스
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSetService {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String SET_KEY_PREFIX = "set:";

    // 10강: SADD로 회원을 추가하고 실제로 새로 추가됐는지 반환한다.
    public boolean addMember(String name, String member) {
        String key = createKey(name);

        Long addedCount =
                setOperations().add(key, member);

        boolean added = addedCount != null && addedCount > 0;

        if (added) {
            log.info(
                    "Redis Set member added. key={}, member={}",
                    key,
                    member
            );
        } else {
            log.debug(
                    "Redis Set member already exists. key={}, member={}",
                    key,
                    member
            );
        }

        return added;
    }

    // 10강: SMEMBERS로 Set의 전체 회원을 조회한다.
    public Optional<Set<String>> findMembers(String name) {
        Set<String> members =
                setOperations().members(createKey(name));

        if (members == null || members.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(members);
    }

    // 10강: SISMEMBER로 특정 회원이 Set에 포함되어 있는지 확인한다.
    public boolean containsMember(String name, String member) {
        Boolean exists =
                setOperations().isMember(createKey(name), member);

        return Boolean.TRUE.equals(exists);
    }

    // 10강: SREM으로 회원을 제거하고 실제로 제거됐는지 반환한다.
    public boolean removeMember(String name, String member) {
        String key = createKey(name);

        Long removedCount =
                setOperations().remove(key, member);

        boolean removed =
                removedCount != null && removedCount > 0;

        if (removed) {
            log.info(
                    "Redis Set member removed. key={}, member={}",
                    key,
                    member
            );
        } else {
            log.debug(
                    "Redis Set member not found. key={}, member={}",
                    key,
                    member
            );
        }

        return removed;
    }

    // 10강: SCARD로 Set에 저장된 회원 수를 조회한다.
    public long countMembers(String name) {
        Long count =
                setOperations().size(createKey(name));

        return count == null ? 0 : count;
    }

    // 10강: SINTER로 두 Set의 교집합을 조회한다.
    public Set<String> intersect(String leftName, String rightName) {
        Set<String> members = setOperations().intersect(
                createKey(leftName),
                createKey(rightName)
        );

        return emptyIfNull(members);
    }

    // 10강: SUNION으로 두 Set의 합집합을 조회한다.
    public Set<String> union(String leftName, String rightName) {
        Set<String> members = setOperations().union(
                createKey(leftName),
                createKey(rightName)
        );

        return emptyIfNull(members);
    }

    // 10강: SDIFF로 왼쪽 Set에만 존재하는 회원을 조회한다.
    public Set<String> difference(String leftName, String rightName) {
        Set<String> members = setOperations().difference(
                createKey(leftName),
                createKey(rightName)
        );

        return emptyIfNull(members);
    }

    private SetOperations<String, String> setOperations() {
        return stringRedisTemplate.opsForSet();
    }

    private String createKey(String name) {
        return SET_KEY_PREFIX + name;
    }

    private Set<String> emptyIfNull(Set<String> members) {
        return members == null ? Set.of() : members;
    }
}
