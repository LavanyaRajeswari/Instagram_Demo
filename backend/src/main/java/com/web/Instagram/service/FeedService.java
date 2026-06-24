package com.web.Instagram.service;

import com.web.Instagram.dto.feed.FeedPostDto;
import com.web.Instagram.entity.Media;
import com.web.Instagram.entity.Post;
import com.web.Instagram.repository.CommentRepository;
import com.web.Instagram.repository.FollowRepository;
import com.web.Instagram.repository.LikeRepository;
import com.web.Instagram.repository.PostRepository;
import com.web.Instagram.repository.SavedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService{

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final SavedPostRepository savedPostRepository;

    @Transactional(readOnly = true)
    public Page<FeedPostDto> getFeed(Long currentUserId, Pageable pageable) {
        List<Long> followingIds = followRepository.findByFollowerId(currentUserId)
                .stream()
                .map(f -> f.getFollowing().getId())
                .collect(Collectors.toList());

        if (!followingIds.contains(currentUserId)) {
            followingIds.add(currentUserId);
        }

        if (followingIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Page<Post> postPage = postRepository.findFeedPostsByUserIds(followingIds, pageable);

        return postPage.map(post -> toFeedDto(post, currentUserId));
    }

    private FeedPostDto toFeedDto(Post post, Long currentUserId) {
        List<String> mediaUrls = post.getMedia().stream()
                .map(Media::getMediaUrl)
                .collect(Collectors.toList());

        long likeCount = likeRepository.countByPostId(post.getId());
        boolean isLiked = likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());
        boolean isSaved = savedPostRepository.existsByUserIdAndPostId(currentUserId, post.getId());

        double score = calculateScore(post, likeCount);

        return FeedPostDto.builder()
                .postId(post.getId())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .fullName(post.getUser().getFullName())
                .profilePicture(post.getUser().getProfilePicture())
                .mediaUrls(mediaUrls)
                .likeCount(likeCount)
                .commentCount(commentRepository.countByPostId(post.getId()))
                .isLiked(isLiked)
                .isSaved(isSaved)
                .score(score)
                .source("FOLLOWED")
                .build();
    }

    private double calculateScore(Post post, long likeCount) {
        return likeCount;
    }
}
