package com.jipi.redis_lab.zset.dto;

public record ZSetMemberRequest(String member,
                                double score) {
}
