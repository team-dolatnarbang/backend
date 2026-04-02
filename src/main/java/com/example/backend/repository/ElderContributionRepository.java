package com.example.backend.repository;

import com.example.backend.domain.contribution.ElderContribution;
import com.example.backend.domain.contribution.ElderContribution.Status;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElderContributionRepository extends JpaRepository<ElderContribution, UUID> {

    // 유적지별로 게시(PUBLISHED)된 시니어 스토리 후보 목록을 조회할 때 사용
    List<ElderContribution> findBySite_IdAndStatus(UUID siteId, Status status);
}

