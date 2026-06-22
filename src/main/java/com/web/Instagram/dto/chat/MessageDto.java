package com.web.Instagram.dto.chat;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long senderId;
    private String content;
    private boolean seen;
    private LocalDateTime createdAt;
}
