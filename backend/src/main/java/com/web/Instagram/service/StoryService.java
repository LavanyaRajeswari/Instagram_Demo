package com.web.Instagram.service;

import com.web.Instagram.dto.story.StoryResponse;
import com.web.Instagram.entity.Hashtag;
import com.web.Instagram.entity.Story;
import com.web.Instagram.entity.StoryLike;
import com.web.Instagram.entity.StoryReply;
import com.web.Instagram.entity.StoryView;
import com.web.Instagram.entity.User;
import com.web.Instagram.entity.StoryArchive;
import com.web.Instagram.entity.StorySticker;
import com.web.Instagram.entity.SavedStory;
import com.web.Instagram.repository.FollowRepository;
import com.web.Instagram.repository.HashtagRepository;
import com.web.Instagram.repository.StoryArchiveRepository;
import com.web.Instagram.repository.StoryLikeRepository;
import com.web.Instagram.repository.StoryReplyRepository;
import com.web.Instagram.repository.StoryRepository;
import com.web.Instagram.repository.StoryMusicRepository;
import com.web.Instagram.repository.StoryStickerRepository;
import com.web.Instagram.repository.StoryViewRepository;
import com.web.Instagram.repository.SavedStoryRepository;
import com.web.Instagram.repository.UserRepository;
import com.web.Instagram.service.HashtagService;
import com.web.Instagram.service.TagService;
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
    private final StoryArchiveRepository storyArchiveRepository;
    private final StoryStickerRepository storyStickerRepository;
    private final SavedStoryRepository savedStoryRepository;
    private final StoryMusicRepository storyMusicRepository;
    private final HashtagService hashtagService;
    private final HashtagRepository hashtagRepository;
    private final TagService tagService;

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

    public List<User> getStoryLikes(Long storyId) {
        Story story = getStoryOrThrow(storyId);
        return storyLikeRepository.findByStory(story).stream()
                .map(StoryLike::getUser)
                .collect(java.util.stream.Collectors.toList());
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

    @Transactional
    public void archiveStory(Long storyId, Long userId) {
        Story story = getStoryOrThrow(storyId);
        if (!story.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        if (!storyArchiveRepository.findByUserIdOrderByArchivedAtDesc(userId).stream()
                .anyMatch(a -> a.getMediaUrl().equals(story.getMediaUrl()))) {
            StoryArchive archive = StoryArchive.builder()
                    .user(story.getUser())
                    .mediaUrl(story.getMediaUrl())
                    .mediaType(story.getMediaType())
                    .caption(story.getCaption())
                    .build();
            storyArchiveRepository.save(archive);
        }
    }

    public List<StoryArchive> getArchivedStories(Long userId) {
        return storyArchiveRepository.findByUserIdOrderByArchivedAtDesc(userId);
    }

    @Transactional
    public void setAudience(Long storyId, String audience, Long userId) {
        Story story = getStoryOrThrow(storyId);
        if (!story.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        if (!java.util.List.of("PUBLIC", "FOLLOWERS", "CLOSE_FRIENDS").contains(audience)) {
            throw new RuntimeException("Invalid audience. Must be PUBLIC, FOLLOWERS, or CLOSE_FRIENDS");
        }
        story.setAudience(audience);
        storyRepository.save(story);
    }

    @Transactional
    public void addSticker(Long storyId, String stickerType, String data, Long userId) {
        Story story = getStoryOrThrow(storyId);
        if (!story.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        StorySticker sticker = StorySticker.builder()
                .story(story)
                .stickerType(stickerType)
                .data(data)
                .build();
        storyStickerRepository.save(sticker);
    }

    public List<StorySticker> getStoryStickers(Long storyId) {
        return storyStickerRepository.findByStoryId(storyId);
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

    @Transactional
    public SavedStory saveStory(Long userId, Long storyId) {
        if (savedStoryRepository.existsByUserIdAndStoryId(userId, storyId)) {
            throw new RuntimeException("Story already saved");
        }
        User user = getUserOrThrow(userId);
        Story story = getStoryOrThrow(storyId);
        return savedStoryRepository.save(SavedStory.builder().user(user).story(story).build());
    }

    @Transactional
    public void unsaveStory(Long userId, Long storyId) {
        savedStoryRepository.deleteByUserIdAndStoryId(userId, storyId);
    }

    public List<SavedStory> getSavedStories(Long userId) {
        return savedStoryRepository.findByUserIdOrderBySavedAtDesc(userId);
    }

    @Transactional
    public void setMusic(Long storyId, Long musicId, Long userId) {
        Story story = getStoryOrThrow(storyId);
        if (!story.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        storyMusicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));
        story.setMusicId(musicId);
        storyRepository.save(story);
    }

    @Transactional
    public void setFontStyle(Long storyId, String fontStyle, Boolean showFontStyle, Long userId) {
        Story story = getStoryOrThrow(storyId);
        if (!story.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        story.setFontStyle(fontStyle);
        if (showFontStyle != null) {
            story.setShowFontStyle(showFontStyle);
        }
        storyRepository.save(story);
    }

    @Transactional
    public List<String> extractHashtagsFromStory(Long storyId) {
        Story story = getStoryOrThrow(storyId);
        List<String> hashtags = hashtagService.extractHashtags(story.getCaption());
        for (String tag : hashtags) {
            Hashtag hashtag = Hashtag.builder()
                    .tag(tag)
                    .postId(storyId)
                    .build();
            hashtagRepository.save(hashtag);
        }
        return hashtags;
    }

    public List<String> extractMentionsFromStory(Long storyId) {
        Story story = getStoryOrThrow(storyId);
        return tagService.extractMentions(story.getCaption());
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