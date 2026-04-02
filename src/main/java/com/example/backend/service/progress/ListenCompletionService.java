package com.example.backend.service.progress;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.domain.progress.ListenCompletion;
import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.domain.site.Site;
import com.example.backend.dto.site.response.CompleteListenResponse;
import com.example.backend.repository.ListenCompletionRepository;
import com.example.backend.repository.SiteRepository;
import com.example.backend.service.session.SessionService;
import com.example.backend.service.site.SiteService;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ListenCompletionService {

    private final SessionService sessionService;
    private final SiteService siteService;
    private final SiteRepository siteRepository;
    private final ListenCompletionRepository listenCompletionRepository;

    public CompleteListenResponse completeListen(UUID sessionId, UUID siteId, Integer durationListenedSec) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(siteId, "siteId");

        AnonymousSession session = sessionService.getOrCreate(sessionId);
        int resetVersion = session.getResetVersion();
        Site site = siteService.getSite(siteId);

        if (listenCompletionRepository.existsBySession_SessionIdAndSite_IdAndResetVersion(
                sessionId, siteId, resetVersion)) {
            return buildResponse(site);
        }

        assertPriorSitesCompleted(sessionId, resetVersion, site);

        ListenCompletion row = new ListenCompletion(
                session, site, resetVersion, Instant.now(), durationListenedSec);
        try {
            listenCompletionRepository.save(row);
        } catch (DataIntegrityViolationException e) {
            if (!listenCompletionRepository.existsBySession_SessionIdAndSite_IdAndResetVersion(
                    sessionId, siteId, resetVersion)) {
                throw e;
            }
        }

        return buildResponse(site);
    }

    private void assertPriorSitesCompleted(UUID sessionId, int resetVersion, Site site) {
        if (site.getOrder() <= 1) {
            return;
        }
        List<Site> ordered = siteRepository.findAllByOrderByOrderAsc();
        for (Site prior : ordered) {
            if (prior.getOrder() >= site.getOrder()) {
                break;
            }
            if (!listenCompletionRepository.existsBySession_SessionIdAndSite_IdAndResetVersion(
                    sessionId, prior.getId(), resetVersion)) {
                throw new ApiException(ErrorCode.SITE_LOCKED);
            }
        }
    }

    private CompleteListenResponse buildResponse(Site site) {
        UUID nextSiteId = siteRepository.findAllByOrderByOrderAsc().stream()
                .filter(s -> s.getOrder() > site.getOrder())
                .map(Site::getId)
                .findFirst()
                .orElse(null);
        return new CompleteListenResponse(site.getId(), true, nextSiteId);
    }

    @Transactional(readOnly = true)
    public boolean hasCompletedAllSites(UUID sessionId, int resetVersion) {
        long total = siteRepository.count();
        if (total == 0) {
            return false;
        }
        long done = listenCompletionRepository.countBySession_SessionIdAndResetVersion(sessionId, resetVersion);
        return done >= total;
    }
}
