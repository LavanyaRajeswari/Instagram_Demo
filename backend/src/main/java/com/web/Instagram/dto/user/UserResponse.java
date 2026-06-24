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
    private String pronouns;
    private boolean isProfessional;
    private boolean isBusiness;
    private boolean isCreator;
    private String category;
    private boolean isEmailVerified;
    private String role;
    private LocalDateTime lastActiveAt;
    private String accountStatus;
    private boolean commentsDisabled;
    private boolean hideLikeCount;
    private boolean activityStatus;
    private boolean readReceipts;
    private boolean messageRequestsEnabled;
    private String sensitiveContentFilter;
    private boolean allowReelDownloads;
    private String theme;
    private boolean storyRepliesEnabled;
    private boolean storyMentionsEnabled;
}