package com.jipi.redis_lab.hash.dto;

import java.util.Map;

public record HashEntriesResponse(
        String name,
        Map<String, String> entries

) {
}
