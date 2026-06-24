package com.web.Instagram.service;

import com.web.Instagram.dto.post.PostResponse;
import com.web.Instagram.entity.Media;
import com.web.Instagram.entity.MediaType;
import com.web.Instagram.entity.Post;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.CommentLikeRepository;
import com.web.Instagram.repository.CommentRepository;
import com.web.Instagram.repository.LikeRepository;
import com.web.Instagram.repository.PostRepository;
import com.web.Instagram.repository.ArchiveRepository;
import com.web.Instagram.repository.PostPinRepository;
import com.web.Instagram.repository.SavedPostRepository;
import com.web.Instagram.repository.ShareRepository;
import com.web.Instagram.repository.UserRepository;
import com.web.Instagram.entity.PostLocationTag;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final LikeRepository likeRepository;
    private final SavedPostRepository savedPostRepository;
    private final ShareRepository shareRepository;
    private final PostPinRepository postPinRepository;
    private final ArchiveRepository archiveRepository;
    private final HashtagService hashtagService;
    private final TagService tagService;
    private final PostLocationTagService postLocationTagService;

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAllPosts(pageable).map(this::toResponse);
    }

    public Page<PostResponse> getFeed(Long userId, Pageable pageable) {
        return postRepository.findFeedPosts(userId, pageable).map(this::toResponse);
    }

    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    public Page<PostResponse> getExplorePosts(Pageable pageable) {
        return postRepository.findExplorePostsByEngagement(pageable).map(this::toResponse);
    }

    public Page<PostResponse> searchPosts(String query, Pageable pageable) {
        return postRepository.searchPosts(query, pageable).map(this::toResponse);
    }

    public Page<PostResponse> getReels(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 30);

        return postRepository.findDistinctByMediaType(
                MediaType.VIDEO,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(this::toResponse);
    }

    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return toResponse(post);
    }

    @Transactional
    public PostResponse createPost(Long userId, String caption, MultipartFile[] files) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (files == null || files.length == 0) {
            throw new RuntimeException("At least one file is required");
        }

        if (files.length > 10) {
            throw new RuntimeException("Maximum 10 files allowed");
        }

        Post post = new Post();
        post.setCaption(caption);
        post.setUser(user);
        List<Media> mediaList = new ArrayList<>();
        int sortOrder = 0;

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue;
            }

            validateFile(file);

            Map<String, Object> result = cloudinaryService.uploadFile(file);

            Media media = new Media();
            media.setMediaUrl(result.get("secure_url").toString());
            media.setPublicId(result.get("public_id").toString());
            media.setMediaType(MediaType.valueOf(file.getContentType().startsWith("video/") ? "VIDEO" : "IMAGE"));
            media.setSortOrder(sortOrder++);
            media.setPost(post);
            mediaList.add(media);
        }

        post.setMedia(mediaList);

        Post saved = postRepository.save(post);
        hashtagService.saveHashtags(caption, saved.getId());
        tagService.saveMentionTags(caption, saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public PostResponse updateCaption(Long postId, String caption, Long userId) {
        Post post = getPostEntity(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        hashtagService.removeHashtagsByPost(postId);
        tagService.removeTagsByPost(postId);
        post.setCaption(caption);
        Post saved = postRepository.save(post);
        hashtagService.saveHashtags(caption, saved.getId());
        tagService.saveMentionTags(caption, saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public PostResponse editPost(Long postId, String caption, MultipartFile[] files) {
        Post post = getPostEntity(postId);
        post.setCaption(caption);

        if (files != null && files.length > 0 && !files[0].isEmpty()) {

            if (files.length > 10) {
                throw new RuntimeException("Maximum 10 files allowed");
            }

            deleteMediaFiles(post);
            List<Media> mediaList = new ArrayList<>();
            int sortOrder = 0;

            for (MultipartFile file : files) {
                validateFile(file);
                Map<String, Object> result = cloudinaryService.uploadFile(file);
                Media media = new Media();
                media.setMediaUrl(result.get("secure_url").toString());
                media.setPublicId(result.get("public_id").toString());
                media.setMediaType(MediaType.valueOf(file.getContentType().startsWith("video/") ? "VIDEO" : "IMAGE"));
                media.setSortOrder(sortOrder++);
                media.setPost(post);

                mediaList.add(media);
            }
            post.getMedia().clear();
            post.getMedia().addAll(mediaList);
        }
        return toResponse(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = getPostEntity(postId);

        hashtagService.removeHashtagsByPost(postId);
        tagService.removeTagsByPost(postId);
        commentLikeRepository.deleteByCommentPostId(postId);
        commentRepository.deleteAll(commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId));
        likeRepository.deleteByPostId(postId);
        savedPostRepository.deleteByPostId(postId);
        shareRepository.deleteByPostId(postId);

        deleteMediaFiles(post);
        post.getMedia().clear();
        postRepository.delete(post);
    }

    public Post getPostEntity(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    private PostResponse toResponse(Post post) {
        List<PostResponse.PostMedia> mediaList = post.getMedia().stream()
                .map(m -> PostResponse.PostMedia.builder()
                        .id(m.getId())
                        .mediaUrl(m.getMediaUrl())
                        .mediaType(m.getMediaType().name())
                        .sortOrder(m.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        User user = post.getUser();

        return PostResponse.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .likeCount(likeRepository.countByPostId(post.getId()))
                .commentCount(commentRepository.countByPostId(post.getId()))
                .saveCount(savedPostRepository.countByPostId(post.getId()))
                .shareCount(shareRepository.countByPostId(post.getId()))
                .visibility(post.getVisibility())
                .hideLikeCount(Boolean.TRUE.equals(post.getHideLikeCount()))
                .commentsDisabled(Boolean.TRUE.equals(post.getCommentsDisabled()))
                .user(PostResponse.PostUser.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .profilePicture(user.getProfilePicture())
                        .createdAt(user.getCreatedAt())
                        .build())
                .media(mediaList)
                .build();
    }

    private void deleteMediaFiles(Post post) {
        for (Media media : post.getMedia()) {
            cloudinaryService.deleteFile(media.getPublicId());
        }
    }

    public Map<String, Object> getPostInsights(Long postId) {
        Post post = getPostEntity(postId);
        Map<String, Object> insights = new java.util.HashMap<>();
        insights.put("id", post.getId());
        insights.put("likeCount", likeRepository.countByPostId(postId));
        insights.put("shareCount", shareRepository.countByPostId(postId));
        insights.put("saveCount", savedPostRepository.countByPostId(postId));
        insights.put("createdAt", post.getCreatedAt());
        return insights;
    }

    public Map<String, Object> getPostAnalytics(Long postId) {
        Post post = getPostEntity(postId);
        Map<String, Object> analytics = new java.util.HashMap<>();
        analytics.put("postId", post.getId());
        analytics.put("likes", likeRepository.countByPostId(postId));
        analytics.put("shares", shareRepository.countByPostId(postId));
        analytics.put("saves", savedPostRepository.countByPostId(postId));
        analytics.put("totalEngagement",
            likeRepository.countByPostId(postId) +
            shareRepository.countByPostId(postId) +
            savedPostRepository.countByPostId(postId));
        analytics.put("hasPinnedPost", postPinRepository.existsByUserIdAndPostId(post.getUser().getId(), postId));
        analytics.put("isArchived", archiveRepository.existsByUserIdAndPostId(post.getUser().getId(), postId));
        return analytics;
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new RuntimeException(
                    "Only image and video files are allowed"
            );
        }
    }

    @Transactional
    public void setVisibility(Long postId, String visibility, Long userId) {
        Post post = getPostEntity(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        if (!java.util.List.of("PUBLIC", "FOLLOWERS", "CLOSE_FRIENDS").contains(visibility)) {
            throw new RuntimeException("Invalid visibility. Must be PUBLIC, FOLLOWERS, or CLOSE_FRIENDS");
        }
        post.setVisibility(visibility);
        postRepository.save(post);
    }

    @Transactional
    public void setHideLikeCount(Long postId, boolean hide, Long userId) {
        Post post = getPostEntity(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        post.setHideLikeCount(hide);
        postRepository.save(post);
    }

    @Transactional
    public void setCommentsDisabled(Long postId, boolean disabled, Long userId) {
        Post post = getPostEntity(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        post.setCommentsDisabled(disabled);
        postRepository.save(post);
    }

    public PostLocationTag addLocationTag(Long postId, String name, String address, String city, String country, Double lat, Double lng, String placeId) {
        return postLocationTagService.addLocationTag(postId, name, address, city, country, lat, lng, placeId);
    }

    public List<PostLocationTag> getLocationTags(Long postId) {
        return postLocationTagService.getPostLocationTags(postId);
    }
}