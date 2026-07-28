package com.jipi.redis_lab.key.dto;

public record KeyExpirationResponse(String key,
                                    KeyExpirationStatus status,
                                    Long remainingSeconds) {

    public static KeyExpirationResponse expiring(
            String key,
            long remainingSeconds
    ) {
        return new KeyExpirationResponse(
                key,
                KeyExpirationStatus.EXPIRING,
                remainingSeconds
        );
    }

    public static KeyExpirationResponse persistent(String key) {
        return new KeyExpirationResponse(
                key,
                KeyExpirationStatus.PERSISTENT,
                null
        );
    }
}
