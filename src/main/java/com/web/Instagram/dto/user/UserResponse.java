package com.web.Instagram.dto.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String bio;
    private String gender;
    private String profilePicture;
    private String website;
    private boolean isPrivate;
    private boolean isVerified;
    private Long postsCount;
    private Long followersCount;
    private Long followingCount;
    private LocalDateTime lastActiveAt;
}
