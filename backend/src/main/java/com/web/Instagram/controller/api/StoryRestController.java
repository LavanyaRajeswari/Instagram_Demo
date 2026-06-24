package com.web.Instagram.controller.api;

import com.web.Instagram.dto.story.StoryResponse;
import com.web.Instagram.entity.StoryReply;
import com.web.Instagram.entity.StoryView;
import com.web.Instagram.service.StoryService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
public class StoryRestController {

    private final StoryService storyService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<StoryResponse>> getActiveStories(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        List<StoryResponse> stories = storyService.getActiveStories(userId);
        return ResponseEntity.ok(stories);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<StoryResponse> createStory(
            Principal principal,
            @RequestParam(required = false) String caption,
            @RequestParam MultipartFile media) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(storyService.createStory(userId, caption, media));
    }

    @PostMapping("/{storyId}/like")
    public ResponseEntity<Void> likeStory(Principal principal, @PathVariable Long storyId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.likeStory(storyId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{storyId}/like")
    public ResponseEntity<Void> unlikeStory(Principal principal, @PathVariable Long storyId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.unlikeStory(storyId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{storyId}/liked")
    public ResponseEntity<Boolean> isLiked(Principal principal, @PathVariable Long storyId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(storyService.isLiked(storyId, userId));
    }

    @GetMapping("/{storyId}/likes")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getLikeCount(storyId));
    }

    @GetMapping("/{storyId}/likes/users")
    public ResponseEntity<List<com.web.Instagram.entity.User>> getStoryLikes(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getStoryLikes(storyId));
    }

    @PostMapping("/{storyId}/reply")
    public ResponseEntity<StoryReply> replyToStory(
            Principal principal,
            @PathVariable Long storyId,
            @RequestParam String text) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(storyService.replyToStory(storyId, userId, text));
    }

    @GetMapping("/{storyId}/replies")
    public ResponseEntity<List<StoryReply>> getReplies(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getReplies(storyId));
    }

    @PostMapping("/{storyId}/view")
    public ResponseEntity<Void> trackView(Principal principal, @PathVariable Long storyId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.trackView(storyId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{storyId}/views")
    public ResponseEntity<List<StoryView>> getStoryViews(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getStoryViews(storyId));
    }

    @GetMapping("/{storyId}/views/count")
    public ResponseEntity<Long> getViewCount(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getViewCount(storyId));
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> deleteStory(Principal principal, @PathVariable Long storyId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.deleteStory(storyId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{storyId}/archive")
    public ResponseEntity<Void> archiveStory(Principal principal, @PathVariable Long storyId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.archiveStory(storyId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/archived")
    public ResponseEntity<List<com.web.Instagram.entity.StoryArchive>> getArchivedStories(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(storyService.getArchivedStories(userId));
    }

    @PutMapping("/{storyId}/audience")
    public ResponseEntity<Void> setStoryAudience(
            Principal principal,
            @PathVariable Long storyId,
            @RequestParam String audience) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.setAudience(storyId, audience, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{storyId}/stickers")
    public ResponseEntity<Void> addSticker(
            Principal principal,
            @PathVariable Long storyId,
            @RequestParam String stickerType,
            @RequestParam String data) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.addSticker(storyId, stickerType, data, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{storyId}/stickers")
    public ResponseEntity<List<com.web.Instagram.entity.StorySticker>> getStoryStickers(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getStoryStickers(storyId));
    }

    @PutMapping("/{storyId}/music")
    public ResponseEntity<Void> setMusic(
            Principal principal,
            @PathVariable Long storyId,
            @RequestParam Long musicId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.setMusic(storyId, musicId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{storyId}/font-style")
    public ResponseEntity<Void> setFontStyle(
            Principal principal,
            @PathVariable Long storyId,
            @RequestParam String fontStyle,
            @RequestParam(required = false, defaultValue = "false") boolean showFontStyle) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        storyService.setFontStyle(storyId, fontStyle, showFontStyle, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{storyId}/save")
    public ResponseEntity<Void> saveStory(Principal principal, @PathVariable Long storyId) {
        storyService.saveStory(
                userService.getCurrentUser(principal.getName()).getId(), storyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{storyId}/save")
    public ResponseEntity<Void> unsaveStory(Principal principal, @PathVariable Long storyId) {
        storyService.unsaveStory(
                userService.getCurrentUser(principal.getName()).getId(), storyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/saved")
    public ResponseEntity<List<com.web.Instagram.entity.SavedStory>> getSavedStories(Principal principal) {
        return ResponseEntity.ok(storyService.getSavedStories(
                userService.getCurrentUser(principal.getName()).getId()));
    }
}