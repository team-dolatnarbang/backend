package com.example.backend.dto.tribute.response;

import java.util.List;

public record TributesResponse(
        List<TributeFeedItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

