package com.web.Instagram.controller.api;

import com.web.Instagram.entity.LiveStream;
import com.web.Instagram.entity.LiveStreamComment;
import com.web.Instagram.entity.LiveStreamReaction;
import com.web.Instagram.entity.LiveStreamViewer;
import com.web.Instagram.service.LiveStreamService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
public class LiveStreamRestController {

    private final LiveStreamService liveStreamService;
    private final UserService userService;

    @GetMapping("/active")
    public ResponseEntity<List<LiveStream>> getActiveStreams() {
        return ResponseEntity.ok(liveStreamService.getActiveStreams());
    }

    @PostMapping
    public ResponseEntity<LiveStream> startStream(Principal principal, @RequestParam(required = false) String title) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(liveStreamService.createStream(userId, title));
    }

    @PostMapping("/{streamId}/end")
    public ResponseEntity<LiveStream> endStream(Principal principal, @PathVariable Long streamId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(liveStreamService.endStream(streamId, userId));
    }

    @PostMapping("/{streamId}/join")
    public ResponseEntity<Void> joinStream(Principal principal, @PathVariable Long streamId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        liveStreamService.joinStream(streamId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{streamId}/leave")
    public ResponseEntity<Void> leaveStream(Principal principal, @PathVariable Long streamId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        liveStreamService.leaveStream(streamId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{streamId}/viewers")
    public ResponseEntity<List<LiveStreamViewer>> getViewers(@PathVariable Long streamId) {
        return ResponseEntity.ok(liveStreamService.getViewers(streamId));
    }

    @GetMapping("/{streamId}/viewer-count")
    public ResponseEntity<Long> getViewerCount(@PathVariable Long streamId) {
        return ResponseEntity.ok(liveStreamService.getViewerCount(streamId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LiveStream>> getMyStreams(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(liveStreamService.getUserStreams(userId));
    }

    @PostMapping("/{streamId}/comment")
    public ResponseEntity<LiveStreamComment> sendComment(Principal principal, @PathVariable Long streamId, @RequestParam String text) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(liveStreamService.sendComment(streamId, userId, text));
    }

    @GetMapping("/{streamId}/comments")
    public ResponseEntity<List<LiveStreamComment>> getComments(@PathVariable Long streamId) {
        return ResponseEntity.ok(liveStreamService.getComments(streamId));
    }

    @PostMapping("/{streamId}/react")
    public ResponseEntity<LiveStreamReaction> sendReaction(Principal principal, @PathVariable Long streamId, @RequestParam String type) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(liveStreamService.sendReaction(streamId, userId, type));
    }

    @DeleteMapping("/{streamId}/react")
    public ResponseEntity<Void> removeReaction(Principal principal, @PathVariable Long streamId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        liveStreamService.removeReaction(streamId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{streamId}/reactions")
    public ResponseEntity<List<LiveStreamReaction>> getReactions(@PathVariable Long streamId) {
        return ResponseEntity.ok(liveStreamService.getReactions(streamId));
    }

    @PostMapping("/{streamId}/moderate")
    public ResponseEntity<LiveStream> moderateStream(Principal principal, @PathVariable Long streamId, @RequestParam boolean disableComments) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(liveStreamService.moderateStream(streamId, userId, disableComments));
    }
}