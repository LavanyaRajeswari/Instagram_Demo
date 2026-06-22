package com.web.Instagram.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String caption;
    private LocalDateTime createdAt;
    private Long likeCount;
    private Long commentCount;
    private PostUser user;
    private List<PostMedia> media;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostUser {
        private Long id;
        private String username;
        private String fullName;
        private String profilePicture;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostMedia {
        private Long id;
        private String mediaUrl;
        private String mediaType;
        private int sortOrder;
    }
}
