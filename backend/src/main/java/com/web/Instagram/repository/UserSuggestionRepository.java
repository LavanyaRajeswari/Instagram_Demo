package com.web.Instagram.repository;

import com.web.Instagram.entity.UserSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserSuggestionRepository extends JpaRepository<UserSuggestion, Long> {
    List<UserSuggestion> findByUserIdAndDismissedFalseOrderByCreatedAtDesc(Long userId);
    List<UserSuggestion> findByUserIdAndSuggestionTypeOrderByCreatedAtDesc(Long userId, String suggestionType);
}