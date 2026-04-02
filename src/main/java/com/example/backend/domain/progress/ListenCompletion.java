package com.example.backend.domain.progress;

import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.domain.site.Site;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Comment;

@Entity
@Table(
        name = "listen_completions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_listen_session_site_version",
                        columnNames = {"session_id", "site_id", "reset_version"}
                )
        }
)
public class ListenCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @Comment("익명 세션(X-Session-Id)")
    private AnonymousSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    @Comment("유적지")
    private Site site;

    @Column(name = "reset_version", nullable = false)
    @Comment("진행 리셋 회차(세션의 resetVersion)")
    private int resetVersion;

    @Column(nullable = false)
    @Comment("청취 완료 시각")
    private Instant completedAt;

    @Column
    @Comment("클라이언트가 보낸 청취 시간(초), 선택")
    private Integer durationListenedSec;

    protected ListenCompletion() {
    }

    public ListenCompletion(
            AnonymousSession session,
            Site site,
            int resetVersion,
            Instant completedAt,
            Integer durationListenedSec
    ) {
        this.session = session;
        this.site = site;
        this.resetVersion = resetVersion;
        this.completedAt = completedAt;
        this.durationListenedSec = durationListenedSec;
    }

    public UUID getId() {
        return id;
    }

    public AnonymousSession getSession() {
        return session;
    }

    public Site getSite() {
        return site;
    }

    public int getResetVersion() {
        return resetVersion;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Integer getDurationListenedSec() {
        return durationListenedSec;
    }
}
