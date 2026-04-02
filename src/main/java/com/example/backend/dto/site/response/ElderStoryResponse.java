package com.example.backend.dto.site.response;

import com.example.backend.domain.contribution.ElderContribution;

public record ElderStoryResponse(String audioUrl) {
    public static ElderStoryResponse from(ElderContribution contribution) {
        return new ElderStoryResponse(contribution.getTtsAudioUrl());
    }
}

