package com.jipi.redis_lab.zset.controller;

import com.jipi.redis_lab.zset.dto.*;
import com.jipi.redis_lab.zset.model.RankedMember;
import com.jipi.redis_lab.zset.service.RedisZSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 11강: Redis Sorted Set 랭킹 기능을 HTTP API로 제공한다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/zsets")
public class RedisZSetController {

    private final RedisZSetService redisZSetService;

    // 11강: 회원의 점수를 등록하거나 변경한다.
    @PostMapping("/{name}/members")
    public ResponseEntity<ZSetMemberResponse> saveMemberScore(
            @PathVariable("name") String name,
            @RequestBody ZSetMemberRequest request
    ) {
        redisZSetService.saveMemberScore(
                name,
                request.member(),
                request.score()
        );

        return redisZSetService
                .findMember(name, request.member())
                .map(member -> ResponseEntity.ok(
                        toResponse(name, member)
                ))
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // 11강: 회원의 점수를 증가하거나 감소시킨다.
    @PatchMapping("/{name}/members/{member}/score")
    public ResponseEntity<ZSetMemberResponse> incrementScore(
            @PathVariable("name") String name,
            @PathVariable("member") String member,
            @RequestBody ZSetScoreIncrementRequest request
    ) {
        redisZSetService.incrementScore(
                name,
                member,
                request.delta()
        );

        return redisZSetService
                .findMember(name, member)
                .map(rankedMember -> ResponseEntity.ok(
                        toResponse(name, rankedMember)
                ))
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // 11강: 특정 회원의 현재 점수와 순위를 조회한다.
    @GetMapping("/{name}/members/{member}")
    public ResponseEntity<ZSetMemberResponse> findMember(
            @PathVariable("name") String name,
            @PathVariable("member") String member
    ) {
        return redisZSetService
                .findMember(name, member)
                .map(rankedMember -> ResponseEntity.ok(
                        toResponse(name, rankedMember)
                ))
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // 11강: 점수가 높은 순서대로 상위 회원을 조회한다.
    @GetMapping("/{name}/top")
    public ResponseEntity<ZSetRankingResponse> findTop(
            @PathVariable("name") String name,
            @RequestParam(
                    name = "limit",
                    defaultValue = "10"
            ) int limit
    ) {
        List<ZSetRankingEntryResponse> rankings =
                redisZSetService.findTop(name, limit)
                        .stream()
                        .map(member ->
                                new ZSetRankingEntryResponse(
                                        member.rank(),
                                        member.member(),
                                        member.score()
                                ))
                        .toList();

        return ResponseEntity.ok(
                new ZSetRankingResponse(
                        name,
                        rankings,
                        rankings.size()
                )
        );
    }

    private ZSetMemberResponse toResponse(
            String name,
            RankedMember member
    ) {
        return new ZSetMemberResponse(
                name,
                member.member(),
                member.score(),
                member.rank()
        );
    }
}