package com.jipi.redis_lab.set.dto;

import java.util.Set;

public record SetMembersResponse(String name,
                                 Set<String> members,
                                 int size) {
}
