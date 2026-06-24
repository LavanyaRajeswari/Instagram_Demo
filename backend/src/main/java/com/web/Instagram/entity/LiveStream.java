package com.web.Instagram.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "live_streams")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LiveStream {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 500)
    private String streamUrl;

    @Column(length = 200)
    private String title;

    @Column(length = 20)
    private String status;

    private Integer viewerCount = 0;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Boolean commentsDisabled = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}