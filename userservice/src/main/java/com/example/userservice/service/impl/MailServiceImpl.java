package com.example.userservice.service.impl;

import com.example.userservice.service.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MailServiceImpl implements MailService {

    JavaMailSender mailSender;

    @Override
    public void sendActivationEmail(String to, String activationLink) {
        String subject = "Kích hoạt tài khoản của bạn";
        String body = String.format("Chào bạn,\n\nĐể kích hoạt tài khoản của bạn, vui lòng nhấp vào liên kết sau:\n%s\n\nNếu bạn không yêu cầu điều này, vui lòng bỏ qua email này.\n\nTrân trọng,\nĐội ngũ tuyển sinh", activationLink);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        String subject = "Đặt lại mật khẩu của bạn";
        String body = String.format("Chào bạn,\n\nBạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấp vào liên kết sau để đặt lại mật khẩu của bạn:\n%s\n\nLiên kết này sẽ hết hạn sau một thời gian ngắn. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\nTrân trọng,\nĐội ngũ tuyển sinh", resetLink);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            // Có thể thêm message.setFrom("your-email@example.com") nếu cần

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage()); // Hoặc xử lý ngoại lệ mềm hơn
        }
    }
}
