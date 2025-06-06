package com.example.userservice.service;

public interface MailService {
    void sendActivationEmail(String to, String activationLink);
    void sendPasswordResetEmail(String to, String resetLink);
    // Có thể thêm các phương thức gửi email chung hơn
    void sendEmail(String to, String subject, String body);
}