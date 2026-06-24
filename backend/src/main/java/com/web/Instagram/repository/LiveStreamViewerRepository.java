package com.web.Instagram.repository;

import com.web.Instagram.entity.LiveStreamViewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LiveStreamViewerRepository extends JpaRepository<LiveStreamViewer, Long> {
    List<LiveStreamViewer> findByLiveStreamId(Long liveStreamId);
    long countByLiveStreamId(Long liveStreamId);
    boolean existsByLiveStreamIdAndUserId(Long liveStreamId, Long userId);
    void deleteByLiveStreamIdAndUserId(Long liveStreamId, Long userId);
    void deleteByLiveStreamId(Long liveStreamId);
}