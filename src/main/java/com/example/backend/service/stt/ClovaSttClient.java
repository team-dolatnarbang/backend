package com.example.backend.service.stt;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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
 * CLOVA Speech Recognition(STT) REST client.
 *
 * Spec: multipart/form-data with "media" and "params", auth header "X-CLOVASPEECH-API-KEY".
 */
@Component
@RequiredArgsConstructor
public class ClovaSttClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${clova.stt.invoke-url}")
    private String invokeUrl;

    @Value("${clova.stt.secret}")
    private String secret;

    public String transcribeKorean(Path audioPath) {
        Objects.requireNonNull(audioPath, "audioPath");
        if (invokeUrl == null || invokeUrl.isBlank() || secret == null || secret.isBlank()) {
            throw new ApiException(
                    ErrorCode.INTERNAL_ERROR,
                    Map.of("reason", "CLOVA_STT_CONFIG_MISSING")
            );
        }
        final String url = Objects.requireNonNull(invokeUrl, "invokeUrl");
        final String apiKey = Objects.requireNonNull(secret, "secret");

        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(audioPath);
        } catch (IOException e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }

        Objects.requireNonNull(bytes, "audioBytes");

        String filename = audioPath.getFileName() == null ? "audio" : audioPath.getFileName().toString();
        ByteArrayResource media = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        // CLOVA STT expects params as JSON string in multipart form.
        // completion=sync returns transcript in response when succeeded.
        String paramsJson;
        try {
            paramsJson = objectMapper.writeValueAsString(Map.of(
                    "language", "ko-KR",
                    "completion", "sync",
                    "fullText", true
            ));
        } catch (IOException e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }

        HttpHeaders paramsHeaders = new HttpHeaders();
        paramsHeaders.setContentType(MediaType.APPLICATION_JSON);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media", media);
        body.add("params", new HttpEntity<>(paramsJson, paramsHeaders));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-CLOVASPEECH-API-KEY", apiKey);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            String responseBody = Objects.requireNonNullElse(response.getBody(), "{}");
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.get("text");
            if (textNode != null && !textNode.isNull()) {
                return textNode.asText();
            }

            // If response doesn't include text, return a safe error.
            throw new ApiException(
                    ErrorCode.INTERNAL_ERROR,
                    Map.of("reason", "CLOVA_STT_NO_TEXT", "result", root.path("result").asText(""))
            );
        } catch (RestClientException | IOException e) {
            throw new ApiException(
                    ErrorCode.INTERNAL_ERROR,
                    Map.of("reason", "CLOVA_STT_REQUEST_FAILED")
            );
        }
    }
}

