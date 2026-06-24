package com.web.Instagram.repository;

import com.web.Instagram.entity.TagControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TagControlRepository extends JpaRepository<TagControl, Long> {
    Optional<TagControl> findByUserIdAndTargetUserId(Long userId, Long targetUserId);
    boolean existsByUserIdAndTargetUserId(Long userId, Long targetUserId);
}