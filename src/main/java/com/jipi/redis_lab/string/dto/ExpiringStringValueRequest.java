package com.jipi.redis_lab.string.dto;

public record ExpiringStringValueRequest(
        String value,
        long ttlSeconds) {
}
