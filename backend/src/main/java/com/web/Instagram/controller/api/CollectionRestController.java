package com.web.Instagram.controller.api;

import com.web.Instagram.entity.Collection;
import com.web.Instagram.service.CollectionService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collections")
public class CollectionRestController {

    private final CollectionService collectionService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Collection>> getUserCollections(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(collectionService.getUserCollections(userId));
    }

    @PostMapping
    public ResponseEntity<Collection> createCollection(Principal principal, @RequestBody Map<String, String> body) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(collectionService.createCollection(userId, body.get("name")));
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<Collection> renameCollection(
            Principal principal,
            @PathVariable Long collectionId,
            @RequestBody Map<String, String> body) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(collectionService.renameCollection(collectionId, body.get("name"), userId));
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(Principal principal, @PathVariable Long collectionId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        collectionService.deleteCollection(collectionId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{collectionId}/posts/{postId}")
    public ResponseEntity<Collection> addPostToCollection(
            Principal principal,
            @PathVariable Long collectionId,
            @PathVariable Long postId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(collectionService.addPostToCollection(collectionId, postId, userId));
    }

    @DeleteMapping("/{collectionId}/posts/{postId}")
    public ResponseEntity<Collection> removePostFromCollection(
            Principal principal,
            @PathVariable Long collectionId,
            @PathVariable Long postId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(collectionService.removePostFromCollection(collectionId, postId, userId));
    }
}