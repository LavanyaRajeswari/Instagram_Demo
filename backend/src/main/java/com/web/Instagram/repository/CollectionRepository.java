package com.web.Instagram.repository;

import com.web.Instagram.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUserIdOrderByUpdatedAtDesc(Long userId);

    long countByUserId(Long userId);
}