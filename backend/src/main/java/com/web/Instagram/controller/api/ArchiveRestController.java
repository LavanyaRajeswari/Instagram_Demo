package com.web.Instagram.controller.api;

import com.web.Instagram.dto.post.PostResponse;
import com.web.Instagram.entity.Archive;
import com.web.Instagram.service.ArchiveService;
import com.web.Instagram.service.PostService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/archive")
public class ArchiveRestController {

    private final ArchiveService archiveService;
    private final PostService postService;
    private final UserService userService;

    @PostMapping("/{postId}")
    public ResponseEntity<Void> archivePost(Principal principal, @PathVariable Long postId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        archiveService.archivePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unarchivePost(Principal principal, @PathVariable Long postId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        archiveService.unarchivePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/status")
    public ResponseEntity<Boolean> isArchived(Principal principal, @PathVariable Long postId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(archiveService.isArchived(userId, postId));
    }

    @GetMapping
    public ResponseEntity<Page<Archive>> getArchivedPosts(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(archiveService.getArchivedPosts(userId, page, size));
    }
}