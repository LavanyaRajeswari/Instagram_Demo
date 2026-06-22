package com.web.Instagram.controller.api;

import com.web.Instagram.dto.user.*;
import com.web.Instagram.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query.trim()));
    }

    @GetMapping("/suggested")
    public ResponseEntity<List<UserResponse>> getSuggestedUsers(
            Principal principal,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(userService.getSuggestedUsers(userId, limit));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(userService.getCurrentUser(
                principal != null ? principal.getName() : null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            Principal principal,
            @RequestBody UpdateRequest request) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PutMapping(value = "/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> updateProfilePicture(
            Principal principal,
            @RequestParam MultipartFile profilePicture) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(userService.updateProfilePicture(userId, profilePicture));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            Principal principal,
            @RequestBody ChangePasswordRequest request) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(Principal principal, @PathVariable Long id) {
        Long currentUserId = userService.getCurrentUser(principal.getName()).getId();
        if (!currentUserId.equals(id)) {
            return ResponseEntity.status(403).build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
