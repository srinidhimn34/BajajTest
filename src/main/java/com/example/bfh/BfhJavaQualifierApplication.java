package com.example.bfh;

import com.example.bfh.dto.FinalQueryRequest;
import com.example.bfh.dto.GenerateWebhookRequest;
import com.example.bfh.dto.GenerateWebhookResponse;
import com.example.bfh.service.WebhookClient;
import com.example.bfh.util.SqlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class BfhJavaQualifierApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BfhJavaQualifierApplication.class);

    private final WebhookClient webhookClient;

    public BfhJavaQualifierApplication(WebhookClient webhookClient) {
        this.webhookClient = webhookClient;
    }

    @Value("${bfh.user.name}")
    private String name;

    @Value("${bfh.user.regNo}")
    private String regNo;

    @Value("${bfh.user.email}")
    private String email;

    @Value("${bfh.endpoints.fallbackSubmit}")
    private String fallbackSubmitUrl;

    public static void main(String[] args) {
        SpringApplication.run(BfhJavaQualifierApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            log.info("Starting BFH Qualifier flow...");

            // 1) Generate webhook + access token
            var req = new GenerateWebhookRequest(name, regNo, email);
            GenerateWebhookResponse resp = webhookClient.generateWebhook(req);
            log.info("Received webhook response. webhook={}, accessTokenPresent={}",
                    resp.getWebhook(), StringUtils.hasText(resp.getAccessToken()));

            // 2) Pick SQL based on last two digits of regNo
            int lastTwo = extractLastTwoDigits(regNo);
            boolean isOdd = (lastTwo % 2) == 1;
            String finalSql = isOdd ? SqlBuilder.buildQuestion1Sql() : SqlBuilder.buildQuestion2Sql();
            log.info("RegNo last two digits = {} -> {} -> picked {}",
                    lastTwo, isOdd ? "odd" : "even", isOdd ? "Question 1" : "Question 2");

            // 3) Submit to webhook (prefer returned 'webhook' URL; fallback to fixed URL)
            String submitUrl = StringUtils.hasText(resp.getWebhook()) ? resp.getWebhook() : fallbackSubmitUrl;

            FinalQueryRequest finalPayload = new FinalQueryRequest(finalSql);

            webhookClient.submitFinalQuery(submitUrl, resp.getAccessToken(), finalPayload);
            log.info("✅ Final query submitted successfully.");

        } catch (Exception ex) {
            log.error("❌ Flow failed: {}", ex.getMessage(), ex);
        }
    }

    private int extractLastTwoDigits(String regNoStr) {
        String digits = regNoStr.replaceAll("\\D+", "");
        if (digits.isEmpty()) return 0;
        String tail = digits.length() >= 2 ? digits.substring(digits.length() - 2) : digits;
        return Integer.parseInt(tail);
    }
}
