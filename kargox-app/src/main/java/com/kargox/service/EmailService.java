package com.kargox.service;

public interface EmailService {

    void sendVerificationEmail(String to, String fullName, String verificationCode);
    void sendPasswordResetEmail(String to, String resetCode);
    void sendBookingNotificationEmail(String to, String subject, String body);
}
