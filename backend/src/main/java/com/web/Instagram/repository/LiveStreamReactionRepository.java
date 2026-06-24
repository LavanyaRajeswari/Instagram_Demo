package com.web.Instagram.repository;

import com.web.Instagram.entity.LiveStreamReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LiveStreamReactionRepository extends JpaRepository<LiveStreamReaction, Long> {
    List<LiveStreamReaction> findByLiveStreamId(Long liveStreamId);
    long countByLiveStreamId(Long liveStreamId);
    boolean existsByLiveStreamIdAndUserId(Long liveStreamId, Long userId);
    void deleteByLiveStreamIdAndUserId(Long liveStreamId, Long userId);
}