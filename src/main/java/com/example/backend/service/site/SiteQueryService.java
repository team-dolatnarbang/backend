package com.example.backend.service.site;

import com.example.backend.domain.contribution.ElderContribution;
import com.example.backend.domain.site.Site;
import com.example.backend.dto.site.response.ElderStoryResponse;
import com.example.backend.dto.site.response.SiteDetailResponse;
import com.example.backend.repository.ElderContributionRepository;
import java.util.List;
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
    private final ElderContributionRepository elderContributionRepository;
    private final Random random = new Random();

    public SiteDetailResponse getSiteDetail(UUID siteId) {
        Objects.requireNonNull(siteId, "siteId");

        Site site = siteService.getSite(siteId);
        ElderStoryResponse elderStory = pickRandomElderStory(siteId);
        return SiteDetailResponse.of(site, elderStory);
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

