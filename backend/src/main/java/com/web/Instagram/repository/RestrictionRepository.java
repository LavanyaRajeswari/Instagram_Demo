package com.web.Instagram.repository;

import com.web.Instagram.entity.Restriction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RestrictionRepository extends JpaRepository<Restriction, Long> {
    Optional<Restriction> findByUserIdAndRestrictedUserId(Long userId, Long restrictedUserId);
    boolean existsByUserIdAndRestrictedUserId(Long userId, Long restrictedUserId);
    List<Restriction> findByUserId(Long userId);
    void deleteByUserIdAndRestrictedUserId(Long userId, Long restrictedUserId);
}