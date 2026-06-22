package com.web.Instagram.repository;

import com.web.Instagram.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatId(
            Long chatId,
            Pageable pageable
    );

    long countByChatIdAndSeenFalse(
            Long chatId
    );

    @Modifying
    @Query("UPDATE Message m SET m.seen = true WHERE m.chat.id = :chatId AND m.seen = false")
    void markAllAsSeen(@Param("chatId") Long chatId);
}