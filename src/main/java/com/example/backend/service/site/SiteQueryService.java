package com.example.backend.service.site;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.domain.contribution.ElderContribution;
import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.domain.site.Site;
import com.example.backend.dto.site.response.ElderStoryResponse;
import com.example.backend.dto.site.response.SiteDetailResponse;
import com.example.backend.repository.ElderContributionRepository;
import com.example.backend.repository.ListenCompletionRepository;
import com.example.backend.repository.SiteRepository;
import com.example.backend.service.session.SessionService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SiteQueryService {

    private final SiteService siteService;
    private final SiteRepository siteRepository;
    private final SessionService sessionService;
    private final ListenCompletionRepository listenCompletionRepository;
    private final ElderContributionRepository elderContributionRepository;
    private final Random random = new Random();

    public SiteDetailResponse getSiteDetail(UUID siteId, UUID sessionId) {
        Objects.requireNonNull(siteId, "siteId");
        Objects.requireNonNull(sessionId, "sessionId");

        Site site = siteService.getSite(siteId);
        AnonymousSession session = sessionService.getOrCreate(sessionId);
        int resetVersion = session.getResetVersion();

        boolean listenCompleted = listenCompletionRepository
                .existsBySession_SessionIdAndResetVersionAndSite_Id(sessionId, resetVersion, siteId);

        boolean unlocked = isUnlocked(site, sessionId, resetVersion);
        if (!unlocked) {
            throw new ApiException(
                    ErrorCode.SITE_LOCKED,
                    Map.of("siteId", siteId.toString(), "order", site.getOrder(), "resetVersion", resetVersion)
            );
        }

        ElderStoryResponse elderStory = pickRandomElderStory(siteId);
        return SiteDetailResponse.of(site, true, listenCompleted, elderStory);
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

    private ElderStoryResponse pickRandomElderStory(UUID siteId) {
        List<ElderContribution> published = elderContributionRepository
                .findBySite_IdAndStatus(siteId, ElderContribution.Status.PUBLISHED);
        if (published.isEmpty()) {
            return null;
        }
        ElderContribution picked = published.get(random.nextInt(published.size()));
        return ElderStoryResponse.from(picked);
    }
}

