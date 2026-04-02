package com.example.backend.service.tribute;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.domain.tribute.FinalTribute;
import com.example.backend.dto.tribute.request.CreateTributeRequest;
import com.example.backend.dto.tribute.response.CreateTributeResponse;
import com.example.backend.dto.tribute.response.TributeFeedItemResponse;
import com.example.backend.dto.tribute.response.TributesResponse;
import com.example.backend.repository.FinalTributeRepository;
import com.example.backend.repository.SiteRepository;
import com.example.backend.service.progress.ListenCompletionService;
import com.example.backend.service.session.SessionService;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TributeService {

    private static final int PLEDGE_UNIT_WON = 1000;
    private static final int NICKNAME_MAX_LEN = 50;
    private static final int MESSAGE_MAX_LEN = 500;

    private final SessionService sessionService;
    private final SiteRepository siteRepository;
    private final FinalTributeRepository finalTributeRepository;
    private final ListenCompletionService listenCompletionService;

    public CreateTributeResponse create(UUID sessionId, CreateTributeRequest request) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(request, "request");

        String nickname = request.nickname() == null ? "" : request.nickname().trim();
        String message = request.message() == null ? "" : request.message().trim();
        if (nickname.isEmpty() || nickname.length() > NICKNAME_MAX_LEN) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (message.isEmpty() || message.length() > MESSAGE_MAX_LEN) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.idempotencyKey() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        AnonymousSession session = sessionService.getOrCreate(sessionId);
        int resetVersion = session.getResetVersion();

        FinalTribute byIdempotencyKey = finalTributeRepository.findByIdempotencyKey(request.idempotencyKey())
                .orElse(null);
        if (byIdempotencyKey != null) {
            return toCreateResponse(byIdempotencyKey);
        }

        FinalTribute existing = finalTributeRepository.findBySession_SessionIdAndResetVersion(sessionId, resetVersion)
                .orElse(null);
        if (existing != null) {
            return toCreateResponse(existing);
        }

        if (!listenCompletionService.hasCompletedAllSites(sessionId, resetVersion)) {
            throw new ApiException(ErrorCode.TRIBUTE_NOT_ALLOWED_YET);
        }

        long totalSites = siteRepository.count();

        int camelliaCount = (int) totalSites;
        int pledgedAmountWon = camelliaCount * PLEDGE_UNIT_WON;

        FinalTribute tribute = new FinalTribute(
                session,
                resetVersion,
                nickname,
                message,
                camelliaCount,
                pledgedAmountWon,
                request.idempotencyKey(),
                Instant.now()
        );

        FinalTribute saved = finalTributeRepository.save(tribute);
        return toCreateResponse(saved);
    }

    @Transactional(readOnly = true)
    public TributesResponse list(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);

        var pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = finalTributeRepository.findAll(pageable);

        List<TributeFeedItemResponse> items = result.getContent().stream()
                .map(TributeFeedItemResponse::from)
                .toList();

        return new TributesResponse(
                items,
                safePage,
                safeSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private CreateTributeResponse toCreateResponse(FinalTribute tribute) {
        return new CreateTributeResponse(
                tribute.getId(),
                tribute.getCamelliaCount(),
                tribute.getPledgedAmountWon(),
                tribute.getCreatedAt()
        );
    }
}

