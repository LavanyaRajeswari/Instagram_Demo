package com.web.Instagram.repository;

import com.web.Instagram.entity.CloseFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloseFriendRepository extends JpaRepository<CloseFriend, Long> {
    List<CloseFriend> findByUserId(Long userId);

    Optional<CloseFriend> findByUserIdAndFriendId(Long userId, Long friendId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    void deleteByUserIdAndFriendId(Long userId, Long friendId);
}