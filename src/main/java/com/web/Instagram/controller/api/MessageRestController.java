package com.web.Instagram.controller.api;

import com.web.Instagram.dto.chat.MessageDto;
import com.web.Instagram.dto.chat.SendMessageRequest;
import com.web.Instagram.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageRestController {

    private final MessageService messageService;

    @GetMapping("/{chatId}")
    public ResponseEntity<Page<MessageDto>> getMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {

        return ResponseEntity.ok(
                messageService.getMessages(
                        chatId,
                        page,
                        size
                )
        );
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(
            @Valid @RequestBody SendMessageRequest request
    ) {

        return ResponseEntity.ok(
                messageService.sendMessage(request)
        );
    }

    @PutMapping("/{chatId}/seen")
    public ResponseEntity<String> markSeen(
            @PathVariable Long chatId
    ) {

        messageService.markSeen(chatId);

        return ResponseEntity.ok(
                "Messages marked as seen"
        );
    }
}