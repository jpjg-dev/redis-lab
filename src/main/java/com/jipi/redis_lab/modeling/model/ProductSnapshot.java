package com.jipi.redis_lab.modeling.model;

// 12강: 여러 Redis 자료구조에 흩어진 상품 데이터를 하나로 표현한다.
public record ProductSnapshot(
        long productId,
        String name,
        long price,
        long viewCount,
        long likeCount
) {
}
