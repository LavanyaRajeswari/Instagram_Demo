package com.web.Instagram.repository;

import com.web.Instagram.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    Optional<BlockedUser> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    List<BlockedUser> findByBlockerId(Long blockerId);

    List<BlockedUser> findByBlockedId(Long blockedId);

    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
