package com.web.Instagram.repository;

import com.web.Instagram.entity.AccountRecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccountRecoveryTokenRepository extends JpaRepository<AccountRecoveryToken, Long> {
    Optional<AccountRecoveryToken> findByTokenAndUsedFalseAndExpiresAtAfter(String token, LocalDateTime now);
    Optional<AccountRecoveryToken> findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);
}