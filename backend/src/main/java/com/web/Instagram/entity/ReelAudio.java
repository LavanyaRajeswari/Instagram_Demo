package com.web.Instagram.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reel_audios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReelAudio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 200)
    private String artist;

    @Column(nullable = false, length = 500)
    private String audioUrl;

    @Column(length = 200)
    private String coverArtUrl;

    @Column(nullable = false)
    private Long durationMs;

    @Column(length = 100)
    private String genre;

    @Column(nullable = false)
    private Boolean isTrending = false;

    private Long usageCount = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;
}