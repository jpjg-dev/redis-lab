package com.jipi.redis_lab.modeling.dto;

import java.util.List;

public record ProductRankingResponse(
        List<ProductRankingEntryResponse> rankings,
        int count
) {
}
