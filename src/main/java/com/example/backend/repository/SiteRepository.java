package com.example.backend.repository;

import com.example.backend.domain.site.Site;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    // 순차 진행에서 "이전 order 유적지"를 찾기 위해 order 값으로 조회할 때 사용
    Optional<Site> findByOrder(int siteOrder);
}

