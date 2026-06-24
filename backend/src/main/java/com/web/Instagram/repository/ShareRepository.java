package com.web.Instagram.repository;

import com.web.Instagram.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareRepository extends JpaRepository<Share, Long> {

    long countByPostId(Long postId);

    long countByStoryId(Long storyId);

    void deleteByPostId(Long postId);

    void deleteByStoryId(Long storyId);
}