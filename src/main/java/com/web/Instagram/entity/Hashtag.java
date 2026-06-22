package com.web.Instagram.entity;
import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "hashtags", indexes = {
    @Index(name = "idx_hashtag_tag", columnList = "tag", unique = true)
})
public class Hashtag implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String tag;

    @Column(nullable = false)
    private Long postId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
