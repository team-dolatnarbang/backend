package com.example.backend.dto.contribution.response;

import com.example.backend.domain.contribution.ElderContribution;
import java.util.UUID;

public record ContributionStatusResponse(
        UUID id,
        UUID siteId,
        String contributorName,
        String status,
        String rawTranscript,
        String correctedText,
        String ttsAudioUrl,
        ContributionStatusError error
) {

    public record ContributionStatusError(String code, String message) {
    }

    public static ContributionStatusResponse from(ElderContribution contribution) {
        ContributionStatusError error = buildError(contribution);
        return new ContributionStatusResponse(
                contribution.getId(),
                contribution.getSite().getId(),
                contribution.getContributorName(),
                contribution.getStatus().name(),
                contribution.getRawTranscript(),
                contribution.getCorrectedText(),
                contribution.getTtsAudioUrl(),
                error
        );
    }

    private static ContributionStatusError buildError(ElderContribution contribution) {
        String code = contribution.getErrorCode();
        String message = contribution.getErrorMessage();
        boolean hasCode = code != null && !code.isBlank();
        boolean hasMessage = message != null && !message.isBlank();
        if (!hasCode && !hasMessage) {
            return null;
        }
        return new ContributionStatusError(
                hasCode ? code : null,
                hasMessage ? message : null
        );
    }
}
