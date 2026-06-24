package com.web.Instagram.repository;

import com.web.Instagram.entity.LiveStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveStreamRepository extends JpaRepository<LiveStream, Long> {
    List<LiveStream> findByStatusOrderByStartedAtDesc(String status);
    List<LiveStream> findByUserIdOrderByStartedAtDesc(Long userId);
    Optional<LiveStream> findByUserIdAndStatus(Long userId, String status);
    long countByUserIdAndStatus(Long userId, String status);
}