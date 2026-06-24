package com.web.Instagram.repository;

import com.web.Instagram.entity.LiveStreamComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LiveStreamCommentRepository extends JpaRepository<LiveStreamComment, Long> {
    List<LiveStreamComment> findByLiveStreamIdOrderByCreatedAtAsc(Long liveStreamId);
    long countByLiveStreamId(Long liveStreamId);
}