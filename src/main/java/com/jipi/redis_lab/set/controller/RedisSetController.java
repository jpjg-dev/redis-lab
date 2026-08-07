package com.jipi.redis_lab.set.controller;

import com.jipi.redis_lab.set.dto.*;
import com.jipi.redis_lab.set.service.RedisSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

// 10강: Redis Set과 집합 연산을 HTTP API로 제공한다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sets")
public class RedisSetController {
    private final RedisSetService redisSetService;

    // 10강: Set에 회원을 추가하고 중복 여부를 응답한다.
    @PostMapping("/{name}/members")
    public ResponseEntity<SetMemberAddResponse> addMember(
            @PathVariable("name") String name,
            @RequestBody SetMemberRequest request
    ) {
        boolean added =
                redisSetService.addMember(name, request.member());

        return ResponseEntity.ok(
                new SetMemberAddResponse(
                        name,
                        request.member(),
                        added
                )
        );
    }

    // 10강: Set의 전체 회원을 조회하고 비어 있으면 404로 응답한다.
    @GetMapping("/{name}/members")
    public ResponseEntity<SetMembersResponse> findMembers(
            @PathVariable("name") String name
    ) {
        return redisSetService.findMembers(name)
                .map(members ->
                        new SetMembersResponse(
                                name,
                                members,
                                members.size()
                        ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 10강: 특정 회원이 Set에 포함되어 있는지 확인한다.
    @GetMapping("/{name}/members/{member}")
    public ResponseEntity<SetMembershipResponse> containsMember(
            @PathVariable("name") String name,
            @PathVariable("member") String member
    ) {
        boolean exists =
                redisSetService.containsMember(name, member);

        return ResponseEntity.ok(
                new SetMembershipResponse(
                        name,
                        member,
                        exists
                )
        );
    }

    // 10강: 특정 회원을 Set에서 제거하고 없으면 404로 응답한다.
    @DeleteMapping("/{name}/members/{member}")
    public ResponseEntity<Void> removeMember(
            @PathVariable("name") String name,
            @PathVariable("member") String member
    ) {
        boolean removed =
                redisSetService.removeMember(name, member);

        if (!removed) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // 10강: SCARD를 이용하여 Set의 회원 수를 조회한다.
    @GetMapping("/{name}/count")
    public ResponseEntity<SetCountResponse> countMembers(
            @PathVariable("name") String name
    ) {
        long count =
                redisSetService.countMembers(name);

        return ResponseEntity.ok(
                new SetCountResponse(name, count)
        );
    }

    // 10강: 두 Set의 교집합을 조회한다.
    @GetMapping("/{leftName}/intersection/{rightName}")
    public ResponseEntity<SetOperationResponse> intersect(
            @PathVariable("leftName") String leftName,
            @PathVariable("rightName") String rightName
    ) {
        Set<String> members =
                redisSetService.intersect(leftName, rightName);

        return ResponseEntity.ok(
                new SetOperationResponse(
                        leftName,
                        rightName,
                        "INTERSECTION",
                        members,
                        members.size()
                )
        );
    }

    // 10강: 두 Set의 합집합을 조회한다.
    @GetMapping("/{leftName}/union/{rightName}")
    public ResponseEntity<SetOperationResponse> union(
            @PathVariable("leftName") String leftName,
            @PathVariable("rightName") String rightName
    ) {
        Set<String> members =
                redisSetService.union(leftName, rightName);

        return ResponseEntity.ok(
                new SetOperationResponse(
                        leftName,
                        rightName,
                        "UNION",
                        members,
                        members.size()
                )
        );
    }

    // 10강: 왼쪽 Set에서 오른쪽 Set을 뺀 차집합을 조회한다.
    @GetMapping("/{leftName}/difference/{rightName}")
    public ResponseEntity<SetOperationResponse> difference(
            @PathVariable("leftName") String leftName,
            @PathVariable("rightName") String rightName
    ) {
        Set<String> members =
                redisSetService.difference(leftName, rightName);

        return ResponseEntity.ok(
                new SetOperationResponse(
                        leftName,
                        rightName,
                        "DIFFERENCE",
                        members,
                        members.size()
                )
        );
    }
}
