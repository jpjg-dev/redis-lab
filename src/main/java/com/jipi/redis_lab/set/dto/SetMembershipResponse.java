package com.jipi.redis_lab.set.dto;

public record SetMembershipResponse(String name,
                                    String member,
                                    boolean exists) {
}
