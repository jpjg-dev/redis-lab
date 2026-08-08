package com.jipi.redis_lab.zset.dto;

public record ZSetMemberResponse(String name,
                                 String member,
                                 double score,
                                 long rank) {
}
