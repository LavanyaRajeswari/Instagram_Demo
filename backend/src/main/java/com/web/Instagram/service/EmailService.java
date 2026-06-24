package com.web.Instagram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendVerificationEmail(String to, String token) {
        log.info("=== EMAIL VERIFICATION ===");
        log.info("To: {}", to);
        log.info("Verification link: http://localhost:8080/api/auth/verify-email?token={}", token);
        log.info("==========================");
    }

    public void sendPasswordResetEmail(String to, String token) {
        log.info("=== PASSWORD RESET ===");
        log.info("To: {}", to);
        log.info("Reset link: http://localhost:3000/reset-password?token={}", token);
        log.info("======================");
    }

}