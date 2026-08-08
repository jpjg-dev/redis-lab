package com.jipi.redis_lab.zset.dto;

import java.util.List;

public record ZSetRankingResponse(String name,
                                  List<ZSetRankingEntryResponse> rankings,
                                  int count) {
}
