package com.web.Instagram.controller.api;

import com.web.Instagram.dto.user.UserResponse;
import com.web.Instagram.service.FollowService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class FollowRestController {

    private final FollowService followService;
    private final UserService userService;

    @PostMapping("/{followingId}/follow")
    public ResponseEntity<Void> followUser(
            Principal principal,
            @PathVariable Long followingId) {
        Long followerId = userService.getCurrentUser(principal.getName()).getId();
        followService.followUser(followerId, followingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{followingId}/follow")
    public ResponseEntity<Void> unfollowUser(
            Principal principal,
            @PathVariable Long followingId) {
        Long followerId = userService.getCurrentUser(principal.getName()).getId();
        followService.unfollowUser(followerId, followingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{followingId}/follow/status")
    public ResponseEntity<Boolean> isFollowing(
            Principal principal,
            @PathVariable Long followingId) {
        Long followerId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(followService.isFollowing(followerId, followingId));
    }

    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowersCount(userId));
    }

    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowingCount(userId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<UserResponse>> getFollowers(@PathVariable Long userId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowersUsers(userId, pageable));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<Page<UserResponse>> getFollowing(@PathVariable Long userId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowingUsers(userId, pageable));
    }
}
