package com.web.Instagram.service;

import com.web.Instagram.dto.story.StoryResponse;
import com.web.Instagram.entity.Story;
import com.web.Instagram.entity.StoryLike;
import com.web.Instagram.entity.StoryReply;
import com.web.Instagram.entity.StoryView;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.FollowRepository;
import com.web.Instagram.repository.StoryLikeRepository;
import com.web.Instagram.repository.StoryReplyRepository;
import com.web.Instagram.repository.StoryRepository;
import com.web.Instagram.repository.StoryViewRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final StoryReplyRepository storyReplyRepository;
    private final StoryViewRepository storyViewRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final FollowRepository followRepository;

    public List<StoryResponse> getActiveStories(Long currentUserId) {
        User currentUser = getUserOrThrow(currentUserId);
        List<User> followedUsers = followRepository.findByFollowerId(currentUserId)
                .stream().map(f -> f.getFollowing()).toList();
        return storyRepository.findActiveStoriesByFollowedUsers(LocalDateTime.now(), followedUsers, currentUser)
                .stream()
                .map(story -> toResponse(story, currentUserId))
                .toList();
    }

    public StoryResponse createStory(Long userId, String caption, MultipartFile media) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String mediaUrl = cloudinaryService.uploadStoryMedia(media);

        String mediaType = media.getContentType() != null && media.getContentType().startsWith("video")
                ? "VIDEO"
                : "IMAGE";

        Story story = Story.builder()
                .user(user)
                .caption(caption)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .build();

        return toResponse(storyRepository.save(story), userId);
    }

    @Transactional
    public void likeStory(Long storyId, Long userId) {
        Story story = getStoryOrThrow(storyId);
        User user = getUserOrThrow(userId);

        if (!storyLikeRepository.existsByStoryAndUser(story, user)) {
            storyLikeRepository.save(
                    StoryLike.builder()
                            .story(story)
                            .user(user)
                            .build()
            );
        }
    }

    @Transactional
    public void unlikeStory(Long storyId, Long userId) {
        Story story = getStoryOrThrow(storyId);
        User user = getUserOrThrow(userId);
        storyLikeRepository.deleteByStoryAndUser(story, user);
    }

    public boolean isLiked(Long storyId, Long userId) {
        return storyLikeRepository.existsByStoryAndUser(
                getStoryOrThrow(storyId),
                getUserOrThrow(userId)
        );
    }

    public long getLikeCount(Long storyId) {
        return storyLikeRepository.countByStory(getStoryOrThrow(storyId));
    }

    public StoryReply replyToStory(Long storyId, Long userId, String text) {
        Story story = getStoryOrThrow(storyId);
        User user = getUserOrThrow(userId);

        StoryReply reply = StoryReply.builder()
                .story(story)
                .user(user)
                .text(text)
                .build();

        return storyReplyRepository.save(reply);
    }

    public List<StoryReply> getReplies(Long storyId) {
        return storyReplyRepository.findByStoryOrderByCreatedAtAsc(getStoryOrThrow(storyId));
    }

    @Transactional
    public void deleteStory(Long storyId, Long userId) {
        Story story = getStoryOrThrow(storyId);

        if (!story.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can delete only your own story");
        }

        storyLikeRepository.deleteByStory(story);
        storyReplyRepository.deleteByStory(story);
        storyViewRepository.deleteByStory(story);
        storyRepository.delete(story);
    }

    @Transactional
    public void trackView(Long storyId, Long userId) {
        if (!storyViewRepository.existsByStoryIdAndUserId(storyId, userId)) {
            Story story = getStoryOrThrow(storyId);
            User user = getUserOrThrow(userId);
            storyViewRepository.save(StoryView.builder()
                .story(story)
                .user(user)
                .build());
        }
    }

    public List<StoryView> getStoryViews(Long storyId) {
        return storyViewRepository.findByStoryIdOrderByViewedAtDesc(storyId);
    }

    public long getViewCount(Long storyId) {
        return storyViewRepository.countByStoryId(storyId);
    }

    private StoryResponse toResponse(Story story, Long currentUserId) {
        User user = story.getUser();
        User currentUser = getUserOrThrow(currentUserId);

        return StoryResponse.builder()
                .id(story.getId())
                .mediaUrl(story.getMediaUrl())
                .mediaType(story.getMediaType())
                .caption(story.getCaption())
                .createdAt(story.getCreatedAt())
                .expiresAt(story.getExpiresAt())
                .user(StoryResponse.StoryUser.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .profilePicture(user.getProfilePicture())
                        .build())
                .likeCount(storyLikeRepository.countByStory(story))
                .likedByCurrentUser(storyLikeRepository.existsByStoryAndUser(story, currentUser))
                .viewCount(storyViewRepository.countByStoryId(story.getId()))
                .build();
    }

    private Story getStoryOrThrow(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}