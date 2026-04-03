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
    private final ClovaTtsClient clovaTtsClient;

    // QUEUED 상태의 업로드를 STT 처리한다.
    @Scheduled(fixedDelayString = "${stt.worker.fixed-delay-ms:5000}")
    @Transactional
    public void runStt() {
        for (ElderContribution queued : contributionService.findQueuedTop10()) {
            try {
                contributionService.markProcessing(queued.getId());

                String transcript = clovaSttClient.transcribeKorean(Path.of(queued.getRawAudioUrl()));
                if (transcript == null || transcript.isBlank()) {
                    contributionService.markFailed(queued.getId(), "STT_EMPTY", "Transcript was empty");
                } else {
                    contributionService.saveTranscript(queued.getId(), transcript);
                }
            } catch (ApiException e) {
                Object reason = e.getDetails().get("reason");
                String message = (reason == null ? "" : ("reason=" + reason + " ")) + e.getMessage();
                contributionService.markFailed(queued.getId(), "STT_FAILED", message);
            } catch (Exception e) {
                contributionService.markFailed(queued.getId(), "STT_FAILED", e.getMessage());
            }
        }
    }

    // STT_DONE 상태의 텍스트를 TTS 처리하여 오디오를 생성한다.
    @Scheduled(fixedDelayString = "${stt.worker.fixed-delay-ms:5000}")
    @Transactional
    public void runTts() {
        for (ElderContribution sttDone : contributionService.findSttDoneTop10()) {
            try {
                String text = sttDone.getRawTranscript();
                if (text == null || text.isBlank()) {
                    contributionService.markFailed(sttDone.getId(), "TTS_EMPTY_INPUT", "No transcript to synthesize");
                    continue;
                }

                String ttsAudioUrl = clovaTtsClient.synthesize(text);
                contributionService.saveTtsAndPublish(sttDone.getId(), ttsAudioUrl);
            } catch (ApiException e) {
                Object reason = e.getDetails().get("reason");
                String message = (reason == null ? "" : ("reason=" + reason + " ")) + e.getMessage();
                contributionService.markFailed(sttDone.getId(), "TTS_FAILED", message);
            } catch (Exception e) {
                contributionService.markFailed(sttDone.getId(), "TTS_FAILED", e.getMessage());
            }
        }
    }
}
