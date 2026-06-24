package com.web.Instagram.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_settings", uniqueConstraints = {
    @UniqueConstraint(columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Boolean pushEnabled = true;

    private Boolean likesEnabled = true;

    private Boolean commentsEnabled = true;

    private Boolean followsEnabled = true;

    private Boolean mentionsEnabled = true;

    private Boolean messagesEnabled = true;

    private Boolean storiesEnabled = true;

    private Boolean liveEnabled = true;
}
