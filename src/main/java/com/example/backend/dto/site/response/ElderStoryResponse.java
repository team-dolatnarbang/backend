package com.example.backend.dto.site.response;

import com.example.backend.domain.contribution.ElderContribution;

public record ElderStoryResponse(
        String text,
        String audioUrl,
        String contributorLabel
) {
    public static ElderStoryResponse from(ElderContribution contribution) {
        return new ElderStoryResponse(
                contribution.getCorrectedText(),
                contribution.getTtsAudioUrl(),
                contribution.getContributorName()
        );
    }
}

