package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String toEmail, String verificationToken) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
        String subject = "Verify your email address";
        String body = "Please click the link below to verify your email address:\n\n" + verificationUrl
                + "\n\nThis link will expire in 24 hours.\n\nIf you did not create an account, please ignore this email.";

        sendEmail(toEmail, subject, body);
    }

    public void sendEmail(String to, String subject, String text) {
        try {
            if (fromEmail == null || fromEmail.isBlank()) {
                log.info("Email not configured. Would send to {}: {} - {}", to, subject, text);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.debug("Verification email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
