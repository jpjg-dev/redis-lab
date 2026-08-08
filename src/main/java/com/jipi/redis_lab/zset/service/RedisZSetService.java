package com.jipi.redis_lab.zset.service;

import com.jipi.redis_lab.zset.model.RankedMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// 11강: Redis Sorted Set을 이용한 점수와 랭킹 관리를 담당한다.
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisZSetService {
    private static final String ZSET_KEY_PREFIX = "zset:";
    private final StringRedisTemplate stringRedisTemplate;

    // 11강: ZADD로 회원의 점수를 등록하거나 변경한다.
    public void saveMemberScore(String name, String member, double score) {
        String key = createKey(name);
        zSetOperations().add(
                key,
                member,
                score
        );

        log.info(
                "Redis ZSet member score saved. key={}, member={}, score={}",
                key,
                member,
                score
        );
    }

    // 11강: ZINCRBY로 회원의 점수를 증가시키거나 감소시킨다.
    public double incrementScore(String name, String member, double delta) {
        String key = createKey(name);
        Double score = zSetOperations().incrementScore(key, member, delta);

        if (score == null) {
            throw new IllegalStateException(
                    "Redis ZSet score increment failed."
            );
        }

        log.info(
                "Redis ZSet member score incremented. key={}, member={}, delta={}, score={}",
                key,
                member,
                delta,
                score
        );

        return score;
    }

    // 11강: ZSCORE와 ZREVRANK로 회원의 점수와 순위를 조회한다.
    public Optional<RankedMember> findMember(
            String name,
            String member
    ) {
        String key = createKey(name);

        Double score =
                zSetOperations().score(key, member);

        Long zeroBasedRank =
                zSetOperations().reverseRank(key, member);

        if (score == null || zeroBasedRank == null) {
            return Optional.empty();
        }

        return Optional.of(
                new RankedMember(
                        member,
                        score,
                        zeroBasedRank + 1
                )
        );
    }

    // 11강: ZREVRANGE를 이용하여 점수가 높은 회원부터 조회한다.
    public List<RankedMember> findTop(
            String name,
            int limit
    ) {
        if (limit <= 0) {
            return List.of();
        }

        String key = createKey(name);

        Set<ZSetOperations.TypedTuple<String>> tuples =
                zSetOperations().reverseRangeWithScores(
                        key,
                        0,
                        limit - 1
                );

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<RankedMember> rankings =
                new ArrayList<>();

        long rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String member = tuple.getValue();
            Double score = tuple.getScore();

            if (member == null || score == null) {
                continue;
            }

            rankings.add(
                    new RankedMember(
                            member,
                            score,
                            rank
                    )
            );

            rank++;
        }

        return rankings;
    }

    private ZSetOperations<String, String> zSetOperations() {
        return stringRedisTemplate.opsForZSet();
    }

    private String createKey(String name) {
        return ZSET_KEY_PREFIX + name;
    }
}
