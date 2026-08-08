package com.jipi.redis_lab.modeling.dto;

public record ProductResponse(long productId,
                              String name,
                              long price,
                              long viewcount,
                              long likeCount) {
}
