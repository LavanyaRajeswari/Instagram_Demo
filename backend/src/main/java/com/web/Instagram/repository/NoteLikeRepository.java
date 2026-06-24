package com.web.Instagram.repository;

import com.web.Instagram.entity.NoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NoteLikeRepository extends JpaRepository<NoteLike, Long> {
    Optional<NoteLike> findByNoteIdAndUserId(Long noteId, Long userId);
    boolean existsByNoteIdAndUserId(Long noteId, Long userId);
    long countByNoteId(Long noteId);
    void deleteByNoteIdAndUserId(Long noteId, Long userId);
}