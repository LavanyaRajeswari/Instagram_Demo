package com.web.Instagram.service;

import com.web.Instagram.entity.UserSuggestion;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.UserSuggestionRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSuggestionService {

    private final UserSuggestionRepository suggestionRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserSuggestion createSuggestion(Long userId, String suggestionType, String suggestionData, String source) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        UserSuggestion cs = UserSuggestion.builder()
            .user(user).suggestionType(suggestionType)
            .suggestionData(suggestionData).source(source).build();
        return suggestionRepository.save(cs);
    }

    public List<UserSuggestion> getUserSuggestions(Long userId) {
        return suggestionRepository.findByUserIdAndDismissedFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void dismissSuggestion(Long suggestionId) {
        suggestionRepository.findById(suggestionId).ifPresent(s -> {
            s.setDismissed(true);
            suggestionRepository.save(s);
        });
    }
}