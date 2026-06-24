package com.web.Instagram.service;

import com.web.Instagram.entity.Post;
import com.web.Instagram.entity.Tag;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.PostRepository;
import com.web.Instagram.repository.TagRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    public List<String> extractMentions(String text) {
        List<String> mentions = new ArrayList<>();
        if (text == null || text.isBlank()) return mentions;
        Matcher matcher = MENTION_PATTERN.matcher(text);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions;
    }

    @Transactional
    public void saveMentionTags(String text, Long postId) {
        List<String> usernames = extractMentions(text);
        for (String username : usernames) {
            userRepository.findByUsername(username).ifPresent(user -> {
                Tag tag = Tag.builder()
                    .post(postRepository.getReferenceById(postId))
                    .user(user)
                    .x(0.0)
                    .y(0.0)
                    .build();
                tagRepository.save(tag);
            });
        }
    }

    @Transactional
    public void tagUserInPost(Long postId, Long userId, Double x, Double y) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Tag tag = Tag.builder()
            .post(post)
            .user(user)
            .x(x)
            .y(y)
            .build();
        tagRepository.save(tag);
    }

    public List<Tag> getPostTags(Long postId) {
        return tagRepository.findByPostId(postId);
    }

    @Transactional
    public void removeTagsByPost(Long postId) {
        tagRepository.deleteByPostId(postId);
    }

    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByUserId(userId);
    }

    public long getUserTagCount(Long userId) {
        return tagRepository.countByUserId(userId);
    }
}