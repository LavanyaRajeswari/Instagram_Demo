package com.web.Instagram.dto.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    private Long id;
    private String text;
    private String color;
    private LocalDateTime createdAt;
    private NoteUser user;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteUser {
        private Long id;
        private String username;
        private String fullName;
        private String profilePicture;
    }
}
