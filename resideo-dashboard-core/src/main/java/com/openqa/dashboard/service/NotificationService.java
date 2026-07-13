package com.openqa.dashboard.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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

    public void sendEmail(String from, String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, subject, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendEmailWithAttachment(String from, String to, String subject, String htmlBody,
                                         String attachmentName, byte[] attachmentData, String mimeType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (attachmentData != null) {
                helper.addAttachment(attachmentName, () -> new java.io.ByteArrayInputStream(attachmentData), mimeType);
            }
            mailSender.send(message);
            log.info("Email with attachment sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email with attachment to {}: {}", to, subject, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
