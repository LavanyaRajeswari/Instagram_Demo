package com.web.Instagram.controller.api;

import com.web.Instagram.dto.feed.FeedPostDto;
import com.web.Instagram.service.FeedService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFeed(
            Principal principal,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        Page<FeedPostDto> feedPage = feedService.getFeed(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", feedPage.getContent());
        response.put("page", feedPage.getNumber());
        response.put("size", feedPage.getSize());
        response.put("totalElements", feedPage.getTotalElements());
        response.put("totalPages", feedPage.getTotalPages());
        response.put("hasNext", feedPage.hasNext());
        response.put("hasPrevious", feedPage.hasPrevious());

        return ResponseEntity.ok(response);
    }
}
