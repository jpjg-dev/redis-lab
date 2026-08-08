package com.jipi.redis_lab.modeling.model;

// 12강: 좋아요 기준 상품 랭킹 데이터를 표현한다.
public record RankedProduct(
        long rank,
        long productId,
        long likeCount
) {
}
