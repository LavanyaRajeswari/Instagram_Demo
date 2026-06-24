package com.web.Instagram.controller.api;

import com.web.Instagram.entity.Restriction;
import com.web.Instagram.service.RestrictionService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class RestrictionRestController {

    private final RestrictionService restrictionService;
    private final UserService userService;

    @PostMapping("/{restrictedId}/restrict")
    public ResponseEntity<Void> restrictUser(Principal principal, @PathVariable Long restrictedId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        restrictionService.restrictUser(userId, restrictedId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{restrictedId}/restrict")
    public ResponseEntity<Void> unrestrictUser(Principal principal, @PathVariable Long restrictedId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        restrictionService.unrestrictUser(userId, restrictedId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{restrictedId}/restrict/status")
    public ResponseEntity<Map<String, Boolean>> isRestricted(Principal principal, @PathVariable Long restrictedId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(Map.of("restricted", restrictionService.isRestricted(userId, restrictedId)));
    }

    @GetMapping("/restricted")
    public ResponseEntity<List<Restriction>> getRestrictedUsers(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(restrictionService.getUserRestrictions(userId));
    }
}