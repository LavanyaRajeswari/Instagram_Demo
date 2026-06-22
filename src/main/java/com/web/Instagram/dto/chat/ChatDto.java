package com.web.Instagram.dto.chat;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    private Long id;
    private Long otherUserId;
    private String username;
    private String profilePicture;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private boolean online;
}
