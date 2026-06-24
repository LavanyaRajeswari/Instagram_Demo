package com.web.Instagram.controller.api;

import com.web.Instagram.entity.Favorite;
import com.web.Instagram.service.FavoriteService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteRestController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<Favorite>> getFavorites(Principal principal, Pageable pageable) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId, pageable));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Favorite> addFavorite(Principal principal, @PathVariable Long postId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(favoriteService.addFavorite(userId, postId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> removeFavorite(Principal principal, @PathVariable Long postId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        favoriteService.removeFavorite(userId, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{postId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(Principal principal, @PathVariable Long postId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(Map.of("favorite", favoriteService.isFavorite(userId, postId)));
    }
}