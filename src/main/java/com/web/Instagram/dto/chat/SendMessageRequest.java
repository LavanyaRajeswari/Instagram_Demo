package com.web.Instagram.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class SendMessageRequest {
    @NotNull
    private Long chatId;

    @NotBlank
    private String content;
}
