package com.example.backend.domain.contribution;

import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.domain.site.Site;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "elder_contributions")
public class ElderContribution {

    public enum Status {
        QUEUED,
        PROCESSING,
        STT_DONE,
        CORRECTED,
        TTS_DONE,
        PUBLISHED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("시니어 음성 기록 ID(contributionId)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    @Comment("유적지")
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @Comment("익명 세션(X-Session-Id). GET /contributions 구현 시 사용")
    private AnonymousSession session;

    @Column(nullable = false, length = 50)
    @Comment("표시 이름(시니어가 입력한 이름)")
    private String contributorName;

    @Column(nullable = false)
    @Comment("클라이언트가 보낸 녹음 길이(초)")
    private int durationSec;

    @Column(nullable = false)
    @Comment("원본 음성 파일 URL")
    private String rawAudioUrl;

    @Column(columnDefinition = "text")
    @Comment("STT 원문 텍스트(관리/디버깅용)")
    private String rawTranscript;

    @Column(columnDefinition = "text")
    @Comment("정제된 구술 텍스트")
    private String correctedText;

    @Column
    @Comment("TTS 오디오 URL")
    private String ttsAudioUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("처리 상태")
    private Status status;

    @Column(length = 50)
    @Comment("실패 코드")
    private String errorCode;

    @Column(columnDefinition = "text")
    @Comment("실패 메시지")
    private String errorMessage;

    @Column(nullable = false)
    @Comment("생성 시각")
    private Instant createdAt;

    protected ElderContribution() {
    }

    public ElderContribution(
            Site site,
            AnonymousSession session,
            String contributorName,
            int durationSec,
            String rawAudioUrl,
            Status status,
            Instant createdAt
    ) {
        this.site = site;
        this.session = session;
        this.contributorName = contributorName;
        this.durationSec = durationSec;
        this.rawAudioUrl = rawAudioUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Site getSite() {
        return site;
    }

    public AnonymousSession getSession() {
        return session;
    }

    public String getContributorName() {
        return contributorName;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public String getRawAudioUrl() {
        return rawAudioUrl;
    }

    public String getRawTranscript() {
        return rawTranscript;
    }

    public void setRawTranscript(String rawTranscript) {
        this.rawTranscript = rawTranscript;
    }

    public String getCorrectedText() {
        return correctedText;
    }

    public void setCorrectedText(String correctedText) {
        this.correctedText = correctedText;
    }

    public String getTtsAudioUrl() {
        return ttsAudioUrl;
    }

    public void setTtsAudioUrl(String ttsAudioUrl) {
        this.ttsAudioUrl = ttsAudioUrl;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

