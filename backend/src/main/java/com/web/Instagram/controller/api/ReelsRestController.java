package com.web.Instagram.controller.api;

import com.web.Instagram.dto.post.PostResponse;
import com.web.Instagram.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reels")
public class ReelsRestController {

    private final PostService postService;

    @GetMapping
    public Page<PostResponse> getReels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.getReels(page, size);
    }

    @GetMapping("/search")
    public Page<PostResponse> searchReels(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.searchPosts(query, org.springframework.data.domain.PageRequest.of(page, size));
    }

}