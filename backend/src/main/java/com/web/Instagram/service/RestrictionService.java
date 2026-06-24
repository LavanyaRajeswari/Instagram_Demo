package com.web.Instagram.service;

import com.web.Instagram.entity.Restriction;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.RestrictionRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestrictionService {

    private final RestrictionRepository restrictionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void restrictUser(Long userId, Long restrictedUserId) {
        if (userId.equals(restrictedUserId)) {
            throw new RuntimeException("Cannot restrict yourself");
        }
        if (restrictionRepository.existsByUserIdAndRestrictedUserId(userId, restrictedUserId)) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User restricted = userRepository.findById(restrictedUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Restriction restriction = Restriction.builder()
                .user(user)
                .restrictedUser(restricted)
                .build();
        restrictionRepository.save(restriction);
    }

    @Transactional
    public void unrestrictUser(Long userId, Long restrictedUserId) {
        restrictionRepository.deleteByUserIdAndRestrictedUserId(userId, restrictedUserId);
    }

    public boolean isRestricted(Long userId, Long restrictedUserId) {
        return restrictionRepository.existsByUserIdAndRestrictedUserId(userId, restrictedUserId);
    }

    public List<Restriction> getUserRestrictions(Long userId) {
        return restrictionRepository.findByUserId(userId);
    }
}