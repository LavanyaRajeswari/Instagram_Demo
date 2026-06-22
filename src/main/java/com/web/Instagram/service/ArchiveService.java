package com.web.Instagram.service;

import com.web.Instagram.entity.Archive;
import com.web.Instagram.entity.Post;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.ArchiveRepository;
import com.web.Instagram.repository.PostRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public void archivePost(Long userId, Long postId) {
        if (archiveRepository.existsByUserIdAndPostId(userId, postId)) return;
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        Archive archive = Archive.builder()
            .user(user)
            .post(post)
            .build();
        archiveRepository.save(archive);
    }

    @Transactional
    public void unarchivePost(Long userId, Long postId) {
        archiveRepository.findByUserIdAndPostId(userId, postId)
            .ifPresent(archiveRepository::delete);
    }

    public boolean isArchived(Long userId, Long postId) {
        return archiveRepository.existsByUserIdAndPostId(userId, postId);
    }

    public Page<Archive> getArchivedPosts(Long userId, int page, int size) {
        return archiveRepository.findByUserIdOrderByArchivedAtDesc(userId, PageRequest.of(page, size));
    }
}
