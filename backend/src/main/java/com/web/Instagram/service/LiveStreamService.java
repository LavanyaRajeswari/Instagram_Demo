package com.web.Instagram.service;

import com.web.Instagram.entity.LiveStream;
import com.web.Instagram.entity.LiveStreamComment;
import com.web.Instagram.entity.LiveStreamReaction;
import com.web.Instagram.entity.LiveStreamViewer;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.LiveStreamCommentRepository;
import com.web.Instagram.repository.LiveStreamReactionRepository;
import com.web.Instagram.repository.LiveStreamRepository;
import com.web.Instagram.repository.LiveStreamViewerRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveStreamService {

    private final LiveStreamRepository liveStreamRepository;
    private final LiveStreamViewerRepository viewerRepository;
    private final UserRepository userRepository;
    private final LiveStreamCommentRepository commentRepository;
    private final LiveStreamReactionRepository reactionRepository;

    @Transactional
    public LiveStream createStream(Long userId, String title) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        liveStreamRepository.findByUserIdAndStatus(userId, "LIVE")
            .ifPresent(s -> { throw new RuntimeException("Already streaming"); });
        LiveStream stream = LiveStream.builder()
            .user(user)
            .title(title)
            .status("LIVE")
            .startedAt(LocalDateTime.now())
            .viewerCount(0)
            .build();
        return liveStreamRepository.save(stream);
    }

    @Transactional
    public LiveStream endStream(Long streamId, Long userId) {
        LiveStream stream = liveStreamRepository.findById(streamId)
            .orElseThrow(() -> new RuntimeException("Stream not found"));
        if (!stream.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        stream.setStatus("ENDED");
        stream.setEndedAt(LocalDateTime.now());
        viewerRepository.deleteByLiveStreamId(streamId);
        return liveStreamRepository.save(stream);
    }

    public List<LiveStream> getActiveStreams() {
        return liveStreamRepository.findByStatusOrderByStartedAtDesc("LIVE");
    }

    public List<LiveStream> getUserStreams(Long userId) {
        return liveStreamRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    @Transactional
    public void joinStream(Long streamId, Long userId) {
        if (!viewerRepository.existsByLiveStreamIdAndUserId(streamId, userId)) {
            LiveStream stream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            viewerRepository.save(LiveStreamViewer.builder()
                .liveStream(stream).user(user).build());
            stream.setViewerCount((int) viewerRepository.countByLiveStreamId(streamId));
            liveStreamRepository.save(stream);
        }
    }

    @Transactional
    public void leaveStream(Long streamId, Long userId) {
        viewerRepository.deleteByLiveStreamIdAndUserId(streamId, userId);
        liveStreamRepository.findById(streamId).ifPresent(stream -> {
            stream.setViewerCount((int) viewerRepository.countByLiveStreamId(streamId));
            liveStreamRepository.save(stream);
        });
    }

    public long getViewerCount(Long streamId) {
        return viewerRepository.countByLiveStreamId(streamId);
    }

    public List<LiveStreamViewer> getViewers(Long streamId) {
        return viewerRepository.findByLiveStreamId(streamId);
    }

    @Transactional
    public LiveStreamComment sendComment(Long streamId, Long userId, String text) {
        LiveStream stream = liveStreamRepository.findById(streamId)
            .orElseThrow(() -> new RuntimeException("Stream not found"));
        if (Boolean.TRUE.equals(stream.getCommentsDisabled())) {
            throw new RuntimeException("Comments are disabled for this stream");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        LiveStreamComment comment = LiveStreamComment.builder()
            .liveStream(stream)
            .user(user)
            .text(text)
            .build();
        return commentRepository.save(comment);
    }

    public List<LiveStreamComment> getComments(Long streamId) {
        return commentRepository.findByLiveStreamIdOrderByCreatedAtAsc(streamId);
    }

    @Transactional
    public LiveStreamReaction sendReaction(Long streamId, Long userId, String reactionType) {
        if (!reactionRepository.existsByLiveStreamIdAndUserId(streamId, userId)) {
            LiveStream stream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            LiveStreamReaction reaction = LiveStreamReaction.builder()
                .liveStream(stream)
                .user(user)
                .reactionType(reactionType)
                .build();
            return reactionRepository.save(reaction);
        }
        throw new RuntimeException("Reaction already exists");
    }

    @Transactional
    public void removeReaction(Long streamId, Long userId) {
        reactionRepository.deleteByLiveStreamIdAndUserId(streamId, userId);
    }

    public List<LiveStreamReaction> getReactions(Long streamId) {
        return reactionRepository.findByLiveStreamId(streamId);
    }

    public long getReactionCount(Long streamId) {
        return reactionRepository.countByLiveStreamId(streamId);
    }

    @Transactional
    public LiveStream moderateStream(Long streamId, Long userId, boolean disableComments) {
        LiveStream stream = liveStreamRepository.findById(streamId)
            .orElseThrow(() -> new RuntimeException("Stream not found"));
        if (!stream.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        stream.setCommentsDisabled(disableComments);
        return liveStreamRepository.save(stream);
    }
}