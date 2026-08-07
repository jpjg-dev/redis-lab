package com.jipi.redis_lab.set.dto;

public record SetMemberAddResponse(String name,
                                   String member,
                                   boolean added) {
}
