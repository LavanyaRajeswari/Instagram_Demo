package com.web.Instagram.entity;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, length = 100)
    private String fullName;

    @JsonIgnore
    @Column(unique = true)
    private String email;

    @JsonIgnore
    @Column(unique = true)
    private String mobileNumber;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private LocalDate birthDate;

    @Column(length = 150)
    private String bio;

    @Column(length = 50)
    private String gender;

    @Column(length = 500)
    private String profilePicture;

    @Column(length = 200)
    private String website;

    private String profilePicturePublicId;

    private Boolean isPrivate = false;

    private Boolean isVerified = false;

    @JsonIgnore
    @OneToMany(mappedBy = "following")
    private List<Follow> followers = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "follower")
    private List<Follow> following = new ArrayList<>();

    @Column(length = 20)
    private String accountStatus;

    @Column(length = 45)
    private String lastActiveIp;

    private LocalDateTime lastActiveAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
