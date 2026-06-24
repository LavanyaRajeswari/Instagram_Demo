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
import java.util.Map;

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

    @GetMapping("/{id}/insights")
    public ResponseEntity<Map<String, Object>> getPostInsights(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostInsights(id));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<Map<String, Object>> getPostAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostAnalytics(id));
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<Void> setVisibility(
            Principal principal,
            @PathVariable Long id,
            @RequestParam String visibility) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        postService.setVisibility(id, visibility, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/comments/disable")
    public ResponseEntity<Void> disableComments(Principal principal, @PathVariable Long id) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        postService.setCommentsDisabled(id, true, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/comments/enable")
    public ResponseEntity<Void> enableComments(Principal principal, @PathVariable Long id) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        postService.setCommentsDisabled(id, false, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/hide-likes")
    public ResponseEntity<Void> hideLikeCount(Principal principal, @PathVariable Long id) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        postService.setHideLikeCount(id, true, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/show-likes")
    public ResponseEntity<Void> showLikeCount(Principal principal, @PathVariable Long id) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        postService.setHideLikeCount(id, false, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/location")
    public ResponseEntity<?> addLocationTag(Principal principal, @PathVariable Long id,
                                            @RequestParam String name,
                                            @RequestParam(required = false) String address,
                                            @RequestParam(required = false) String city,
                                            @RequestParam(required = false) String country,
                                            @RequestParam(required = false) Double latitude,
                                            @RequestParam(required = false) Double longitude,
                                            @RequestParam(required = false) String placeId) {
        return ResponseEntity.ok(postService.addLocationTag(id, name, address, city, country, latitude, longitude, placeId));
    }

    @GetMapping("/{id}/location")
    public ResponseEntity<?> getLocationTags(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getLocationTags(id));
    }

}