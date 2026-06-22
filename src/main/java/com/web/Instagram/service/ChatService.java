package com.web.Instagram.service;

import com.web.Instagram.dto.chat.ChatDto;
import com.web.Instagram.entity.Chat;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.ChatRepository;
import com.web.Instagram.repository.MessageRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ChatDto startChat(Long targetUserId) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Chat> existing = chatRepository.findExistingChat(
                currentUser.getId(),
                targetUserId
        );

        if (existing.isPresent()) {
            return convert(existing.get(), currentUser.getId());
        }

        User targetUser = userRepository
                .findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Chat chat = new Chat();
        chat.setUserOne(currentUser);
        chat.setUserTwo(targetUser);

        chat = chatRepository.save(chat);

        return convert(chat, currentUser.getId());
    }

    public List<ChatDto> getChats() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatRepository
                .findByUserOneIdOrUserTwoIdOrderByLastMessageAtDesc(
                        currentUser.getId(),
                        currentUser.getId()
                )
                .stream()
                .map(chat -> convert(chat, currentUser.getId()))
                .toList();
    }

    private ChatDto convert(Chat chat, Long currentUserId) {

        User otherUser;

        if (chat.getUserOne().getId().equals(currentUserId)) {
            otherUser = chat.getUserTwo();
        } else {
            otherUser = chat.getUserOne();
        }

        long unreadCount = messageRepository.countByChatIdAndSeenFalse(
                chat.getId()
        );

        ChatDto dto = new ChatDto();

        dto.setId(chat.getId());
        dto.setOtherUserId(otherUser.getId());
        dto.setUsername(otherUser.getUsername());
        dto.setProfilePicture(otherUser.getProfilePicture());
        dto.setLastMessage(chat.getLastMessage());
        dto.setLastMessageAt(chat.getLastMessageAt());
        dto.setUnreadCount(unreadCount);

        return dto;
    }
}