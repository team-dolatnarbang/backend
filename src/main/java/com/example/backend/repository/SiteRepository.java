package com.example.backend.repository;

import com.example.backend.domain.site.Site;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, UUID> {
}

