package com.web.Instagram.service;

import com.web.Instagram.dto.comment.CommentResponse;
import com.web.Instagram.entity.Comment;
import com.web.Instagram.entity.CommentLike;
import com.web.Instagram.entity.Post;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.CommentLikeRepository;
import com.web.Instagram.repository.CommentRepository;
import com.web.Instagram.repository.PostRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse addComment(Long userId, Long postId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Comment cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setText(text.trim());

        Comment saved = commentRepository.save(comment);

        notificationService.createNotification(
                post.getUser().getId(), userId, "COMMENT", postId, saved.getId(), text
        );

        return toResponse(saved, userId);
    }

    @Transactional
    public CommentResponse addReply(Long userId, Long postId, Long parentCommentId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Reply cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        if (!parentComment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Parent comment does not belong to this post");
        }

        Comment reply = new Comment();
        reply.setUser(user);
        reply.setPost(post);
        reply.setParentComment(parentComment);
        reply.setText(text.trim());

        Comment saved = commentRepository.save(reply);

        notificationService.createNotification(
                parentComment.getUser().getId(), userId, "COMMENT_REPLY", postId, saved.getId(), text
        );

        return toResponse(saved, userId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId, Long currentUserId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);
        return comments.stream().map(comment -> toResponse(comment, currentUserId)).toList();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(Long parentCommentId, Long currentUserId) {
        List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentCommentId);
        return replies.stream().map(reply -> toResponse(reply, currentUserId)).toList();
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is required");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (postId != null && !comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found for this post");
        }

        Long commentOwnerId = comment.getUser().getId();
        Long postOwnerId = comment.getPost().getUser().getId();

        if (!userId.equals(commentOwnerId) && !userId.equals(postOwnerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this comment");
        }

        deleteCommentLikes(comment);
        commentRepository.delete(comment);
    }

    public long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    @Transactional
    public void likeComment(Long userId, Long commentId) {
        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        CommentLike like = new CommentLike();
        like.setUser(user);
        like.setComment(comment);
        commentLikeRepository.save(like);
    }

    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
    }

    public long getCommentLikeCount(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

    public boolean isCommentLiked(Long userId, Long commentId) {
        return commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
    }

    private CommentResponse toResponse(Comment comment, Long currentUserId) {
        long likeCount = commentLikeRepository.countByCommentId(comment.getId());
        boolean likedByCurrentUser = currentUserId != null && commentLikeRepository.existsByUserIdAndCommentId(currentUserId, comment.getId());

        User user = comment.getUser();
        CommentResponse.CommentUser commentUser = CommentResponse.CommentUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profilePicture(user.getProfilePicture())
                .build();

        List<CommentResponse> replies = null;
        if (comment.getReplies() != null) {
            replies = comment.getReplies().stream()
                    .map(reply -> toResponse(reply, currentUserId))
                    .toList();
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .likeCount(likeCount)
                .likedByCurrentUser(likedByCurrentUser)
                .user(commentUser)
                .replies(replies)
                .build();
    }

    private void deleteCommentLikes(Comment comment) {
        if (comment.getReplies() != null) {
            comment.getReplies().forEach(this::deleteCommentLikes);
        }
        commentLikeRepository.deleteByCommentId(comment.getId());
    }
}
