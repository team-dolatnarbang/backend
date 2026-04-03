package com.example.backend.service.contribution;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.domain.contribution.ElderContribution;
import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.domain.site.Site;
import com.example.backend.dto.contribution.response.ContributionStatusResponse;
import com.example.backend.dto.contribution.response.ContributionUploadResponse;
import com.example.backend.repository.ElderContributionRepository;
import com.example.backend.service.session.SessionService;
import com.example.backend.service.site.SiteService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContributionService {

    private final SiteService siteService;
    private final SessionService sessionService;
    private final ElderContributionRepository elderContributionRepository;

    public ContributionUploadResponse upload(
            String contributorName,
            UUID siteId,
            int durationSec,
            String rawAudioUrl,
            UUID sessionId
    ) {
        if (contributorName == null || contributorName.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        Objects.requireNonNull(siteId, "siteId");
        if (durationSec <= 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (rawAudioUrl == null || rawAudioUrl.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        Site site = siteService.getSite(siteId);
        AnonymousSession session = sessionId == null ? null : sessionService.getOrCreate(sessionId);

        ElderContribution contribution = new ElderContribution(
                site,
                session,
                contributorName,
                durationSec,
                rawAudioUrl,
                ElderContribution.Status.QUEUED,
                Instant.now()
        );

        ElderContribution saved = elderContributionRepository.save(contribution);
        return new ContributionUploadResponse(
                saved.getId(),
                saved.getStatus().name(),
                saved.getSite().getId(),
                saved.getContributorName()
        );
    }

    @Transactional(readOnly = true)
    public ContributionStatusResponse getStatus(UUID contributionId) {
        Objects.requireNonNull(contributionId, "contributionId");

        ElderContribution contribution = elderContributionRepository.findById(contributionId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        Map.of("contributionId", contributionId.toString())
                ));

        return ContributionStatusResponse.from(contribution);
    }

    @Transactional(readOnly = true)
    public List<ElderContribution> findQueuedTop10() {
        return elderContributionRepository.findTop10ByStatusOrderByCreatedAtAsc(ElderContribution.Status.QUEUED);
    }

    @Transactional(readOnly = true)
    public List<ElderContribution> findSttDoneTop10() {
        return elderContributionRepository.findTop10ByStatusOrderByCreatedAtAsc(ElderContribution.Status.STT_DONE);
    }

    public ElderContribution markProcessing(UUID contributionId) {
        Objects.requireNonNull(contributionId, "contributionId");
        String contributionIdValue = contributionId.toString();
        ElderContribution contribution = elderContributionRepository.findById(contributionId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        Map.of("contributionId", contributionIdValue)
                ));

        contribution.setStatus(ElderContribution.Status.PROCESSING);
        return contribution;
    }

    public ElderContribution saveTranscript(UUID contributionId, String rawTranscript) {
        Objects.requireNonNull(contributionId, "contributionId");
        String contributionIdValue = contributionId.toString();
        ElderContribution contribution = elderContributionRepository.findById(contributionId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        Map.of("contributionId", contributionIdValue)
                ));

        contribution.setRawTranscript(rawTranscript);
        contribution.setStatus(ElderContribution.Status.STT_DONE);
        contribution.setErrorCode(null);
        contribution.setErrorMessage(null);
        return elderContributionRepository.save(contribution);
    }

    public ElderContribution saveTtsAndPublish(UUID contributionId, String ttsAudioUrl) {
        Objects.requireNonNull(contributionId, "contributionId");
        String contributionIdValue = contributionId.toString();
        ElderContribution contribution = elderContributionRepository.findById(contributionId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        Map.of("contributionId", contributionIdValue)
                ));

        contribution.setTtsAudioUrl(ttsAudioUrl);
        contribution.setStatus(ElderContribution.Status.PUBLISHED);
        contribution.setErrorCode(null);
        contribution.setErrorMessage(null);
        return elderContributionRepository.save(contribution);
    }

    @Transactional(readOnly = true)
    public ElderContribution getPublishedForPlayback(UUID contributionId) {
        Objects.requireNonNull(contributionId, "contributionId");
        String contributionIdValue = contributionId.toString();
        ElderContribution contribution = elderContributionRepository.findById(contributionId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        Map.of("contributionId", contributionIdValue)
                ));
        if (contribution.getStatus() != ElderContribution.Status.PUBLISHED) {
            throw new ApiException(ErrorCode.NOT_FOUND, Map.of("contributionId", contributionIdValue));
        }
        return contribution;
    }

    public ElderContribution markFailed(UUID contributionId, String errorCode, String errorMessage) {
        Objects.requireNonNull(contributionId, "contributionId");
        String contributionIdValue = contributionId.toString();
        ElderContribution contribution = elderContributionRepository.findById(contributionId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        Map.of("contributionId", contributionIdValue)
                ));

        contribution.setStatus(ElderContribution.Status.FAILED);
        contribution.setErrorCode(errorCode);
        contribution.setErrorMessage(errorMessage);
        return contribution;
    }
}

