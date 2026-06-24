package com.web.Instagram.repository;

import com.web.Instagram.entity.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArchiveRepository extends JpaRepository<Archive, Long> {
    Optional<Archive> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Page<Archive> findByUserIdOrderByArchivedAtDesc(Long userId, Pageable pageable);

    void deleteByPostId(Long postId);
}