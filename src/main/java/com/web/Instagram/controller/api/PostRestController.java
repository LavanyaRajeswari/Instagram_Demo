package com.web.Instagram.controller.api;

import com.web.Instagram.dto.post.PostResponse;
import com.web.Instagram.service.PostService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostRestController {

    private final PostService postService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(Principal principal, @PageableDefault(size = 20) Pageable pageable) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(postService.getFeed(userId, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getUserPosts(@PathVariable Long userId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.getUserPosts(userId, pageable));
    }

    @GetMapping("/explore")
    public ResponseEntity<Page<PostResponse>> getExplorePosts(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.getExplorePosts(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(@RequestParam String query, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.searchPosts(query.trim(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> createPost(
            Principal principal,
            @RequestParam(required = false) String caption,
            @RequestParam MultipartFile[] images) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(postService.createPost(userId, caption, images));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> editPost(
            Principal principal,
            @PathVariable Long id,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) MultipartFile[] images) {
        PostResponse post = postService.getPost(id);
        if (!post.getUser().getId().equals(userService.getCurrentUser(principal.getName()).getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(postService.editPost(id, caption, images));
    }

    @PutMapping("/{id}/caption")
    public ResponseEntity<PostResponse> updateCaption(
            Principal principal,
            @PathVariable Long id,
            @RequestParam String caption) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(postService.updateCaption(id, caption, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            Principal principal,
            @PathVariable Long id) {
        PostResponse post = postService.getPost(id);
        if (!post.getUser().getId().equals(userService.getCurrentUser(principal.getName()).getId())) {
            return ResponseEntity.status(403).build();
        }
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }
}
