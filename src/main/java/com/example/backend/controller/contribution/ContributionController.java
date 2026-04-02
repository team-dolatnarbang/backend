package com.example.backend.controller.contribution;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.common.response.ApiResponse;
import com.example.backend.domain.contribution.ElderContribution;
import com.example.backend.dto.contribution.response.ContributionStatusResponse;
import com.example.backend.dto.contribution.response.ContributionUploadResponse;
import com.example.backend.service.contribution.ContributionService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/contributions")
@RequiredArgsConstructor
public class ContributionController {

    private final ContributionService contributionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 시니어 녹음 업로드 API - 업로드 후 contributionId 반환
    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ContributionUploadResponse>> uploadAudio(
            @RequestParam String contributorName,
            @RequestParam String siteId,
            @RequestParam MultipartFile audio,
            @RequestParam int durationSec,
            @RequestHeader(name = "X-Session-Id", required = false) String sessionId
    ) {
        if (audio == null || audio.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        UUID parsedSiteId;
        UUID parsedSessionId = null;
        try {
            parsedSiteId = UUID.fromString(siteId);
            if (sessionId != null && !sessionId.isBlank()) {
                parsedSessionId = UUID.fromString(sessionId);
            }
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        // PVC에 마운트된 디렉토리(file.upload-dir)로 음성 파일을 저장하고, 저장된 경로를 rawAudioUrl로 기록한다.
        String rawAudioUrl = saveAudioFile(audio);

        ContributionUploadResponse body = contributionService.upload(
                contributorName,
                parsedSiteId,
                durationSec,
                rawAudioUrl,
                parsedSessionId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(body));
    }

    // PUBLISHED 상태인 원본 녹음 파일만 스트리밍한다.
    @GetMapping("/{contributionId}/audio")
    public ResponseEntity<Resource> streamPublishedAudio(@PathVariable String contributionId) {
        UUID parsedContributionId;
        try {
            parsedContributionId = UUID.fromString(contributionId);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        ElderContribution contribution = contributionService.getPublishedForPlayback(parsedContributionId);
        Path path = Path.of(contribution.getRawAudioUrl()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new ApiException(ErrorCode.NOT_FOUND, Map.of("contributionId", contributionId));
        }

        Path baseDir = Path.of(uploadDir).toAbsolutePath().normalize();
        if (!path.startsWith(baseDir)) {
            throw new ApiException(ErrorCode.NOT_FOUND, Map.of("contributionId", contributionId));
        }

        FileSystemResource resource = new FileSystemResource(path);
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String probed = Files.probeContentType(path);
            if (probed != null && !probed.isBlank()) {
                contentType = MediaType.parseMediaType(probed);
            }
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(resource);
    }

    // 시니어 구술 처리 상태 조회 API
    @GetMapping("/{contributionId}")
    public ResponseEntity<ApiResponse<ContributionStatusResponse>> getStatus(@PathVariable String contributionId) {
        UUID parsedContributionId;
        try {
            parsedContributionId = UUID.fromString(contributionId);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        ContributionStatusResponse body = contributionService.getStatus(parsedContributionId);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    private String saveAudioFile(MultipartFile audio) {
        try {
            Path baseDir = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(baseDir);

            String filename = UUID.randomUUID() + "-" + (audio.getOriginalFilename() == null ? "audio" : audio.getOriginalFilename());
            Path destination = baseDir.resolve(filename).normalize();
            if (!destination.startsWith(baseDir)) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR);
            }

            try (var in = audio.getInputStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return destination.toString();
        } catch (IOException e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }
}

