package com.example.backend.service.progress;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.domain.progress.ListenCompletion;
import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.domain.site.Site;
import com.example.backend.dto.site.request.CompleteListenRequest;
import com.example.backend.dto.site.response.CompleteListenResponse;
import com.example.backend.repository.ListenCompletionRepository;
import com.example.backend.repository.SiteRepository;
import com.example.backend.service.session.SessionService;
import com.example.backend.service.site.SiteService;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ListenService {

    private final SiteService siteService;
    private final SiteRepository siteRepository;
    private final SessionService sessionService;
    private final ListenCompletionRepository listenCompletionRepository;

    public CompleteListenResponse completeListen(UUID siteId, UUID sessionId, CompleteListenRequest request) {
        Objects.requireNonNull(siteId, "siteId");
        Objects.requireNonNull(sessionId, "sessionId");

        Site site = siteService.getSite(siteId);
        AnonymousSession session = sessionService.getOrCreate(sessionId);
        int resetVersion = session.getResetVersion();

        if (!isUnlocked(site, sessionId, resetVersion)) {
            throw new ApiException(
                    ErrorCode.SITE_LOCKED,
                    Map.of("siteId", siteId.toString(), "order", site.getOrder(), "resetVersion", resetVersion)
            );
        }

        UUID nextSiteId = getNextSiteId(site);

        boolean alreadyCompleted = listenCompletionRepository
                .existsBySession_SessionIdAndResetVersionAndSite_Id(sessionId, resetVersion, siteId);
        if (alreadyCompleted) {
            return CompleteListenResponse.completed(siteId, nextSiteId);
        }

        Integer durationListenedSec = request == null ? null : request.durationListenedSec();
        ListenCompletion completion = new ListenCompletion(
                session,
                site,
                resetVersion,
                Instant.now(),
                durationListenedSec
        );
        listenCompletionRepository.save(completion);

        return CompleteListenResponse.completed(siteId, nextSiteId);
    }

    private boolean isUnlocked(Site site, UUID sessionId, int resetVersion) {
        int order = site.getOrder();
        if (order <= 1) {
            return true;
        }

        Site prevSite = siteRepository.findByOrder(order - 1)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.INTERNAL_ERROR,
                        Map.of("missingSiteOrder", order - 1)
                ));

        return listenCompletionRepository
                .existsBySession_SessionIdAndResetVersionAndSite_Id(sessionId, resetVersion, prevSite.getId());
    }

    private UUID getNextSiteId(Site site) {
        return siteRepository.findByOrder(site.getOrder() + 1)
                .map(Site::getId)
                .orElse(null);
    }
}

