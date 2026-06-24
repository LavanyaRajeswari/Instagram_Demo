package com.web.Instagram.controller.api;

import com.web.Instagram.entity.PostDraft;
import com.web.Instagram.service.DraftService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
public class DraftRestController {

    private final DraftService draftService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<PostDraft>> getDrafts(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(draftService.getUserDrafts(userId));
    }

    @PostMapping
    public ResponseEntity<PostDraft> createDraft(Principal principal,
                                                  @RequestParam(required = false) String caption,
                                                  @RequestParam(required = false) String mediaUrls,
                                                  @RequestParam(required = false) String visibility,
                                                  @RequestParam(required = false) String location) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(draftService.createDraft(userId, caption, mediaUrls, visibility, location));
    }

    @PutMapping("/{draftId}")
    public ResponseEntity<PostDraft> updateDraft(Principal principal,
                                                  @PathVariable Long draftId,
                                                  @RequestParam(required = false) String caption,
                                                  @RequestParam(required = false) String mediaUrls,
                                                  @RequestParam(required = false) String visibility,
                                                  @RequestParam(required = false) String location) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(draftService.updateDraft(draftId, userId, caption, mediaUrls, visibility, location));
    }

    @DeleteMapping("/{draftId}")
    public ResponseEntity<Void> deleteDraft(Principal principal, @PathVariable Long draftId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        draftService.deleteDraft(draftId, userId);
        return ResponseEntity.ok().build();
    }
}