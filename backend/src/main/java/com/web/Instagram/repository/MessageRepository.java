package com.web.Instagram.repository;

import com.web.Instagram.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    @Query("select m from Message m where m.chat.id = :chatId and m.deleted = false order by m.createdAt desc")
    Page<Message> findActiveByChatId(@Param("chatId") Long chatId, Pageable pageable);

    @Query("select m from Message m where m.chat.id = :chatId and m.messageType in :types and m.deleted = false order by m.createdAt desc")
    Page<Message> findByChatIdAndMessageTypeIn(@Param("chatId") Long chatId, @Param("types") List<String> types, Pageable pageable);

    long countBySenderIdAndSeenFalseAndChatId(Long senderId, Long chatId);

    @Query("select m from Message m where m.chat.id = :chatId and lower(m.content) like lower(concat('%', :query, '%')) and m.deleted = false order by m.createdAt desc")
    Page<Message> searchMessages(@Param("chatId") Long chatId, @Param("query") String query, Pageable pageable);

    @Query("select m from Message m where m.chat.id = :chatId and m.forwarded = true and m.deleted = false order by m.createdAt desc")
    Page<Message> findForwardedMessages(@Param("chatId") Long chatId, Pageable pageable);

    @Query("select m from Message m where m.chat.id = :chatId and m.mediaUrl is not null and m.deleted = false order by m.createdAt desc")
    Page<Message> findMediaMessages(@Param("chatId") Long chatId, Pageable pageable);
}