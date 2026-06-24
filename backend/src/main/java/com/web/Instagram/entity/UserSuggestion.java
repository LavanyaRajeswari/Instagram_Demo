package com.web.Instagram.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_suggestions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSuggestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String suggestionType;

    @Column(columnDefinition = "TEXT")
    private String suggestionData;

    @Column(length = 500)
    private String source;

    private Boolean dismissed = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}