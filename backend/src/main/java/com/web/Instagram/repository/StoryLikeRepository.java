package com.web.Instagram.repository;

import com.web.Instagram.entity.Story;
import com.web.Instagram.entity.StoryLike;
import com.web.Instagram.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
    boolean existsByStoryAndUser(Story story, User user);

    void deleteByStoryAndUser(Story story, User user);

    void deleteByStory(Story story);

    long countByStory(Story story);

    List<StoryLike> findByStory(Story story);
}