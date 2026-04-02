package com.example.backend.domain.tribute;

import com.example.backend.domain.session.AnonymousSession;
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
        name = "final_tributes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_final_tribute_session_version",
                        columnNames = {"session_id", "reset_version"}
                ),
                @UniqueConstraint(
                        name = "uk_final_tribute_idempotency_key",
                        columnNames = {"idempotency_key"}
                )
        }
)
public class FinalTribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("마지막 헌화 ID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @Comment("익명 세션(X-Session-Id)")
    private AnonymousSession session;

    @Column(name = "reset_version", nullable = false)
    @Comment("진행 리셋 회차(세션의 resetVersion)")
    private int resetVersion;

    @Column(nullable = false, length = 50)
    @Comment("표시 닉네임(중복 허용)")
    private String nickname;

    @Column(nullable = false, length = 500)
    @Comment("헌화 메시지")
    private String message;

    @Column(nullable = false)
    @Comment("이번 헌화로 반영된 동백 송이 수(회차 완료로 모은 꽃잎 합)")
    private int camelliaCount;

    @Column(nullable = false)
    @Comment("이번 헌화로 적립된 금액(원)")
    private int pledgedAmountWon;

    @Column(name = "idempotency_key", nullable = false, columnDefinition = "uuid")
    @Comment("중복 요청 방지 키")
    private UUID idempotencyKey;

    @Column(nullable = false)
    @Comment("생성 시각")
    private Instant createdAt;

    protected FinalTribute() {
    }

    public FinalTribute(
            AnonymousSession session,
            int resetVersion,
            String nickname,
            String message,
            int camelliaCount,
            int pledgedAmountWon,
            UUID idempotencyKey,
            Instant createdAt
    ) {
        this.session = session;
        this.resetVersion = resetVersion;
        this.nickname = nickname;
        this.message = message;
        this.camelliaCount = camelliaCount;
        this.pledgedAmountWon = pledgedAmountWon;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public AnonymousSession getSession() {
        return session;
    }

    public int getResetVersion() {
        return resetVersion;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public int getCamelliaCount() {
        return camelliaCount;
    }

    public int getPledgedAmountWon() {
        return pledgedAmountWon;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

