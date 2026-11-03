package com.kargox.service.impl;

import com.kargox.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {       //see the README for how to point this at a real mail provider

    private final JavaMailSender mailSender;

    @Value("${kargox.mail.enabled:false}")
    private boolean mailEnabled;

    @Override
    @Async("taskExecutor")
    public void sendVerificationEmail(String to, String fullName, String verificationCode) {

        send(to, "Verify your KargoX account",
                   "Hi " + fullName + ",\n\nYour verification code is: " + verificationCode
                         + "\n\nWelcome to KargoX - Move Anything. Anywhere. Anytime.");
    }

    @Override
    @Async("taskExecutor")
    public void sendPasswordResetEmail(String to, String resetCode) {

        send(to, "Reset your KargoX password",
                   "Your password reset code is: " + resetCode
                         + "\nIf you didn't request this, ignore this email.");
    }

    @Override
    @Async("taskExecutor")
    public void sendBookingNotificationEmail(String to, String subject, String body) {

        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {

        if (!mailEnabled) {
            log.info("[MAIL DISABLED] To: {} | Subject: {} | Body: {}", to, subject, body);

            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
