package com.example.backend.service.stt;

import com.example.backend.common.error.ApiException;
import com.example.backend.domain.contribution.ElderContribution;
import com.example.backend.service.contribution.ContributionService;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ContributionSttScheduler {

    private final ContributionService contributionService;
    private final ClovaSttClient clovaSttClient;

    // QUEUED 상태의 업로드를 주기적으로 STT 처리한다. (운영에서는 모니터링/재시도 정책 필요)
    @Scheduled(fixedDelayString = "${stt.worker.fixed-delay-ms:5000}")
    @Transactional
    public void runOnce() {
        for (ElderContribution queued : contributionService.findQueuedTop10()) {
            try {
                contributionService.markProcessing(queued.getId());

                String transcript = clovaSttClient.transcribeKorean(Path.of(queued.getRawAudioUrl()));
                contributionService.markSttDone(queued.getId(), transcript);
            } catch (ApiException e) {
                Object reason = e.getDetails().get("reason");
                String message = (reason == null ? "" : ("reason=" + reason + " ")) + e.getMessage();
                contributionService.markFailed(queued.getId(), "STT_FAILED", message);
            } catch (Exception e) {
                contributionService.markFailed(queued.getId(), "STT_FAILED", e.getMessage());
            }
        }
    }
}

