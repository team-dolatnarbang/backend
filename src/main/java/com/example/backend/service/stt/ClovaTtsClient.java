package com.example.backend.service.stt;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * CLOVA Voice TTS REST client.
 *
 * POST application/x-www-form-urlencoded with auth headers.
 * Returns audio binary (mp3).
 */
@Component
@RequiredArgsConstructor
public class ClovaTtsClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${clova.tts.invoke-url}")
    private String invokeUrl;

    @Value("${clova.tts.client-id}")
    private String clientId;

    @Value("${clova.tts.client-secret}")
    private String clientSecret;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Convert text to speech and save the resulting mp3 to the upload directory.
     *
     * @return absolute path of the saved TTS audio file
     */
    public String synthesize(String text) {
        Objects.requireNonNull(text, "text");
        if (invokeUrl == null || invokeUrl.isBlank()
                || clientId == null || clientId.isBlank()
                || clientSecret == null || clientSecret.isBlank()) {
            throw new ApiException(
                    ErrorCode.INTERNAL_ERROR,
                    Map.of("reason", "CLOVA_TTS_CONFIG_MISSING")
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("speaker", "nara");
        body.add("volume", "0");
        body.add("speed", "0");
        body.add("pitch", "0");
        body.add("format", "mp3");
        body.add("text", text);

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    invokeUrl,
                    new HttpEntity<>(body, headers),
                    byte[].class
            );

            byte[] audioBytes = response.getBody();
            if (audioBytes == null || audioBytes.length == 0) {
                throw new ApiException(
                        ErrorCode.INTERNAL_ERROR,
                        Map.of("reason", "CLOVA_TTS_EMPTY_RESPONSE")
                );
            }

            return saveAudio(audioBytes);
        } catch (RestClientException e) {
            throw new ApiException(
                    ErrorCode.INTERNAL_ERROR,
                    Map.of("reason", "CLOVA_TTS_REQUEST_FAILED", "message", e.getMessage())
            );
        }
    }

    private String saveAudio(byte[] audioBytes) {
        try {
            Path baseDir = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(baseDir);

            String filename = "tts-" + UUID.randomUUID() + ".mp3";
            Path destination = baseDir.resolve(filename).normalize();

            Files.write(destination, audioBytes);
            return destination.toString();
        } catch (IOException e) {
            throw new ApiException(
                    ErrorCode.INTERNAL_ERROR,
                    Map.of("reason", "CLOVA_TTS_SAVE_FAILED")
            );
        }
    }
}
