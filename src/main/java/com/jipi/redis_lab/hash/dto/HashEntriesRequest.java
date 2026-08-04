package com.jipi.redis_lab.hash.dto;

import java.util.Map;

public record HashEntriesRequest(Map<String, String> entries) {
}
