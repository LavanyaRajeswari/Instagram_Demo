package com.web.Instagram.controller.api;

import com.web.Instagram.dto.post.PostResponse;
import com.web.Instagram.service.HashtagService;
import com.web.Instagram.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hashtags")
public class HashtagRestController {

    private final HashtagService hashtagService;
    private final PostService postService;

    @GetMapping("/{tag}/posts")
    public ResponseEntity<Page<PostResponse>> getPostsByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Long> postIds = hashtagService.getPostIdsByTag(tag, page, size);
        List<PostResponse> posts = postIds.getContent().stream()
            .map(postService::getPost)
            .toList();
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(posts, postIds.getPageable(), postIds.getTotalElements()));
    }

    @GetMapping("/{tag}/count")
    public ResponseEntity<Long> getPostCountByTag(@PathVariable String tag) {
        return ResponseEntity.ok(hashtagService.getPostCountByTag(tag));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingHashtags(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(hashtagService.getTrendingHashtags(limit));
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> searchHashtags(@RequestParam String query) {
        return ResponseEntity.ok(hashtagService.searchHashtags(query));
    }
}