package com.web.Instagram.controller.api;

import com.web.Instagram.dto.chat.ChatDto;
import com.web.Instagram.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @PostMapping("/start/{userId}")
    public ResponseEntity<ChatDto> startChat(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                chatService.startChat(userId)
        );
    }

    @GetMapping
    public ResponseEntity<List<ChatDto>> getChats() {

        return ResponseEntity.ok(
                chatService.getChats()
        );
    }
}