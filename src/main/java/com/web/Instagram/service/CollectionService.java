package com.web.Instagram.service;

import com.web.Instagram.entity.Collection;
import com.web.Instagram.entity.Post;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.CollectionRepository;
import com.web.Instagram.repository.PostRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public List<Collection> getUserCollections(Long userId) {
        return collectionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public Collection createCollection(Long userId, String name) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Collection collection = Collection.builder()
            .name(name)
            .user(user)
            .build();
        return collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(Long collectionId, Long userId) {
        Collection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new RuntimeException("Collection not found"));
        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        collectionRepository.delete(collection);
    }

    @Transactional
    public Collection addPostToCollection(Long collectionId, Long postId, Long userId) {
        Collection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new RuntimeException("Collection not found"));
        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!collection.getPosts().contains(post)) {
            collection.getPosts().add(post);
        }
        return collectionRepository.save(collection);
    }

    @Transactional
    public Collection removePostFromCollection(Long collectionId, Long postId, Long userId) {
        Collection collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new RuntimeException("Collection not found"));
        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        collection.getPosts().removeIf(p -> p.getId().equals(postId));
        return collectionRepository.save(collection);
    }
}
