package com.example.backend.repository;

import com.example.backend.domain.site.Site;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    List<Site> findAllByOrderByOrderAsc();

    Optional<Site> findByOrder(int siteOrder);
}

