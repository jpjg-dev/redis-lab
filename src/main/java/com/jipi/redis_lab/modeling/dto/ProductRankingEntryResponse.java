package com.jipi.redis_lab.modeling.dto;

public record ProductRankingEntryResponse(
        long rank,
        long productId,
        long likeCount
) {
}
