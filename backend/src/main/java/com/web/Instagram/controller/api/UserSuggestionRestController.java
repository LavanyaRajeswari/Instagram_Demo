package com.web.Instagram.controller.api;

import com.web.Instagram.entity.UserSuggestion;
import com.web.Instagram.service.UserSuggestionService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user-suggestions")
@RequiredArgsConstructor
public class UserSuggestionRestController {

    private final UserSuggestionService suggestionService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserSuggestion>> getSuggestions(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(suggestionService.getUserSuggestions(userId));
    }

    @PostMapping
    public ResponseEntity<UserSuggestion> createSuggestion(Principal principal,
                                                               @RequestParam String suggestionType,
                                                               @RequestParam String suggestionData,
                                                               @RequestParam(required = false) String source) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(suggestionService.createSuggestion(userId, suggestionType, suggestionData, source));
    }

    @PostMapping("/{suggestionId}/dismiss")
    public ResponseEntity<Void> dismissSuggestion(@PathVariable Long suggestionId) {
        suggestionService.dismissSuggestion(suggestionId);
        return ResponseEntity.ok().build();
    }
}