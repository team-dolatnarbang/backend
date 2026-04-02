package com.example.backend.dto.tribute.request;

import java.util.UUID;

public record CreateTributeRequest(
        String message,
        String nickname,
        UUID idempotencyKey
) {
}

