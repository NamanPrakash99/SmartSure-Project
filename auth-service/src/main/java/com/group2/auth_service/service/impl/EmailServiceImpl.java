package com.group2.auth_service.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.group2.auth_service.service.EmailService;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("no-reply@smartsure.com", "SmartSure Support");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, false);
            
            mailSender.send(message);
            logger.info("📧 Email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendOtpEmail(String email, String otp) {
        String subject = "SmartSure - Your OTP Verification Code";
        
        String plainContent = "SmartSure - Email Verification\n\n" +
                "Use the verification code below to continue:\n\n" +
                "Code: " + otp + "\n\n" +
                "This code is valid for 10 minutes.\n" +
                "If you didn't request this, you can safely ignore this email.";

        sendEmail(email, subject, plainContent);
    }

    public void sendResetPasswordEmail(String email, String token) {
        String subject = "SmartSure - Password Reset Request";
        String plainContent = "SmartSure - Password Reset\n\n" +
                "You requested to reset your password. Use the following code:\n\n" +
                "Code: " + token + "\n\n" +
                "If you did not request this, please ignore this email.";
        sendEmail(email, subject, plainContent);
    }
}
