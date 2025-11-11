package com.example.bfh.service;

import com.example.bfh.dto.FinalQueryRequest;
import com.example.bfh.dto.GenerateWebhookRequest;
import com.example.bfh.dto.GenerateWebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookClient {

    private static final Logger log = LoggerFactory.getLogger(WebhookClient.class);

    private final RestTemplate restTemplate;

    @Value("${bfh.endpoints.generate}")
    private String generateUrl;

    public WebhookClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GenerateWebhookResponse generateWebhook(GenerateWebhookRequest payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(payload, headers);

        log.debug("POST {}", generateUrl);
        ResponseEntity<GenerateWebhookResponse> response = restTemplate.exchange(
                generateUrl, HttpMethod.POST, entity, GenerateWebhookResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to generate webhook. HTTP=" + response.getStatusCode());
        }
        return response.getBody();
    }

    public void submitFinalQuery(String submitUrl, String accessToken, FinalQueryRequest finalQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        HttpEntity<FinalQueryRequest> entity = new HttpEntity<>(finalQuery, headers);
        log.debug("POST {} with Authorization token present={}", submitUrl, (accessToken != null && !accessToken.isBlank()));

        try {
            ResponseEntity<String> resp = restTemplate.exchange(submitUrl, HttpMethod.POST, entity, String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Submit failed. HTTP=" + resp.getStatusCode() + " body=" + resp.getBody());
            }
            log.info("Submit response: HTTP={} body={}", resp.getStatusCode(), resp.getBody());
        } catch (HttpStatusCodeException ex) {
            log.error("Submit failed. HTTP={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
