package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@surveillance.com}")
    private String fromEmail;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML format
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        // Build HTML content from template
        String htmlContent = buildHtmlFromTemplate(templateName, variables);
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendAlertEmail(String to, String alertTitle, String cameraName, String severity) {
        String subject = String.format("[%s] Alert: %s", severity, alertTitle);
        String htmlContent = buildAlertEmailHtml(alertTitle, cameraName, severity);
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendAiDetectionEmail(String to, String eventType, String cameraName, String confidence) {
        String subject = String.format("AI Detection: %s on %s", eventType, cameraName);
        String htmlContent = buildAiDetectionEmailHtml(eventType, cameraName, confidence);
        sendHtmlEmail(to, subject, htmlContent);
    }

    private String buildHtmlFromTemplate(String templateName, Map<String, Object> variables) {
        // Simple template engine - replace variables
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>").append(templateName).append("</h2>");
        
        variables.forEach((key, value) -> {
            html.append("<p><strong>").append(key).append(":</strong> ")
                .append(value).append("</p>");
        });
        
        html.append("</body></html>");
        return html.toString();
    }

    private String buildAlertEmailHtml(String alertTitle, String cameraName, String severity) {
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .header { background-color: #f44336; color: white; padding: 10px; }
                    .content { padding: 20px; }
                    .severity { font-weight: bold; color: #f44336; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>Camera Surveillance Alert</h2>
                </div>
                <div class="content">
                    <p><strong>Alert:</strong> %s</p>
                    <p><strong>Camera:</strong> %s</p>
                    <p class="severity"><strong>Severity:</strong> %s</p>
                    <p><strong>Time:</strong> %s</p>
                </div>
            </body>
            </html>
            """, alertTitle, cameraName, severity, java.time.LocalDateTime.now());
    }

    private String buildAiDetectionEmailHtml(String eventType, String cameraName, String confidence) {
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .header { background-color: #2196F3; color: white; padding: 10px; }
                    .content { padding: 20px; }
                    .confidence { font-weight: bold; color: #2196F3; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>AI Detection Event</h2>
                </div>
                <div class="content">
                    <p><strong>Event Type:</strong> %s</p>
                    <p><strong>Camera:</strong> %s</p>
                    <p class="confidence"><strong>Confidence:</strong> %s</p>
                    <p><strong>Time:</strong> %s</p>
                </div>
            </body>
            </html>
            """, eventType, cameraName, confidence, java.time.LocalDateTime.now());
    }
}
