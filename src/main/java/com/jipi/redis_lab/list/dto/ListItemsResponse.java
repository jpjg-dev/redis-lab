package com.jipi.redis_lab.list.dto;

import java.util.List;

public record ListItemsResponse(String value, List<String> items, int size) {
}
