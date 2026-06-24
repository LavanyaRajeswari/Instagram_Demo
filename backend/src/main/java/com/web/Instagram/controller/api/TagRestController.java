package com.web.Instagram.controller.api;

import com.web.Instagram.entity.Tag;
import com.web.Instagram.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagRestController {

    private final TagService tagService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Tag>> getPostTags(@PathVariable Long postId) {
        return ResponseEntity.ok(tagService.getPostTags(postId));
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<Void> tagUserInPost(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Double x = body.get("x") != null ? Double.valueOf(body.get("x").toString()) : 0.0;
        Double y = body.get("y") != null ? Double.valueOf(body.get("y").toString()) : 0.0;
        tagService.tagUserInPost(postId, userId, x, y);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Tag>> getUserTags(@PathVariable Long userId) {
        return ResponseEntity.ok(tagService.getUserTags(userId));
    }
}