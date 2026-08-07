package com.jipi.redis_lab.set.dto;

import java.util.Set;

public record SetOperationResponse(String leftName,
                                   String rightName,
                                   String operation,
                                   Set<String> members,
                                   int size) {
}
