package com.jipi.redis_lab.zset.model;

public record RankedMember(String member,
                           double score,
                           long rank) {
}
