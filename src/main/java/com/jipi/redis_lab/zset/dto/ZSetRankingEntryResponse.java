package com.jipi.redis_lab.zset.dto;

public record ZSetRankingEntryResponse(long rank,
                                       String member,
                                       double score) {
}
