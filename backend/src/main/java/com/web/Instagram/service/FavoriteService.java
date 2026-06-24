package com.web.Instagram.service;

import com.web.Instagram.entity.Favorite;
import com.web.Instagram.entity.Post;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.FavoriteRepository;
import com.web.Instagram.repository.PostRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public Favorite addFavorite(Long userId, Long postId) {
        if (favoriteRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("Already in favorites");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        Favorite fav = Favorite.builder().user(user).post(post).build();
        return favoriteRepository.save(fav);
    }

    @Transactional
    public void removeFavorite(Long userId, Long postId) {
        favoriteRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public Page<Favorite> getUserFavorites(Long userId, Pageable pageable) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public boolean isFavorite(Long userId, Long postId) {
        return favoriteRepository.existsByUserIdAndPostId(userId, postId);
    }
}