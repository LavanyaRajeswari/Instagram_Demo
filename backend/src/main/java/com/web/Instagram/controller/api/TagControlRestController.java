package com.web.Instagram.controller.api;

import com.web.Instagram.entity.TagControl;
import com.web.Instagram.service.TagControlService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/tag-controls")
@RequiredArgsConstructor
public class TagControlRestController {

    private final TagControlService tagControlService;
    private final UserService userService;

    @PutMapping("/{targetUserId}")
    public ResponseEntity<TagControl> setTagControl(Principal principal,
                                                     @PathVariable Long targetUserId,
                                                     @RequestParam(required = false) String allowTagging,
                                                     @RequestParam(required = false) String allowMention) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(tagControlService.setTagControl(userId, targetUserId, allowTagging, allowMention));
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<TagControl> getTagControl(Principal principal, @PathVariable Long targetUserId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        TagControl tc = tagControlService.getTagControl(userId, targetUserId);
        if (tc == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(tc);
    }
}