package com.resideo.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendTeams(String webhookUrl, String title, String message, String color) {
        try {
            String payload = """
                {
                    "@type": "MessageCard",
                    "@context": "http://schema.org/extensions",
                    "themeColor": "%s",
                    "title": "%s",
                    "text": "%s"
                }
                """.formatted(color, escape(title), escape(message));
            var client = java.net.http.HttpClient.newHttpClient();
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            client.send(request, java.net.http.HttpResponse.BodyHandlers.discarding());
            log.info("Teams notification sent to {}", webhookUrl);
        } catch (Exception e) {
            log.warn("Failed to send Teams notification", e);
        }
    }

    public void sendEmail(String smtpHost, int smtpPort, String from, String to, String subject, String body) {
        log.info("Email notification would be sent to {}: {}", to, subject);
        // Placeholder — use JavaMailSender in production
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
