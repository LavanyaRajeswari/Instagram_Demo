package com.web.Instagram.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tag_controls", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "target_user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TagControl {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Column(length = 20)
    private String allowTagging;

    @Column(length = 20)
    private String allowMention;

    @CreationTimestamp
    private LocalDateTime createdAt;
}