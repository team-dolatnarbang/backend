package com.example.backend.service.site;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.domain.site.Site;
import com.example.backend.repository.SiteRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public List<Site> listSites() {
        return siteRepository.findAll();
    }

    public Site getSite(UUID siteId) {
        Objects.requireNonNull(siteId, "siteId");
        String siteIdValue = siteId.toString();
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        Map.of("siteId", siteIdValue)
                ));
    }
}

