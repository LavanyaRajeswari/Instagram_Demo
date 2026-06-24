package com.web.Instagram.service;

import com.web.Instagram.entity.AccountRecoveryToken;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.AccountRecoveryTokenRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountRecoveryService {

    private final AccountRecoveryTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String generateRecoveryToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with this email"));
        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        AccountRecoveryToken recoveryToken = AccountRecoveryToken.builder()
            .user(user)
            .token(token)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();
        tokenRepository.save(recoveryToken);
        return token;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        return tokenRepository.findByTokenAndUsedFalseAndExpiresAtAfter(token, LocalDateTime.now())
            .map(recoveryToken -> {
                recoveryToken.setUsed(true);
                tokenRepository.save(recoveryToken);
                User user = recoveryToken.getUser();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }).orElse(false);
    }

    public boolean validateToken(String token) {
        return tokenRepository.findByTokenAndUsedFalseAndExpiresAtAfter(token, LocalDateTime.now()).isPresent();
    }
}