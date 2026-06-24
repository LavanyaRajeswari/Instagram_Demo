package com.web.Instagram.service;

import com.web.Instagram.entity.ChatSetting;
import com.web.Instagram.entity.GroupChat;
import com.web.Instagram.entity.GroupChatAdmin;
import com.web.Instagram.entity.GroupChatMessage;
import com.web.Instagram.entity.GroupChatMessageReaction;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.ChatSettingRepository;
import com.web.Instagram.repository.GroupChatAdminRepository;
import com.web.Instagram.repository.GroupChatMessageRepository;
import com.web.Instagram.repository.GroupChatMessageReactionRepository;
import com.web.Instagram.repository.GroupChatRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final GroupChatAdminRepository groupChatAdminRepository;
    private final GroupChatMessageReactionRepository groupChatMessageReactionRepository;
    private final UserRepository userRepository;
    private final ChatSettingRepository chatSettingRepository;

    @Transactional
    public GroupChat createGroup(String name, String description, List<Long> memberIds, String profilePicture) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> members = new ArrayList<>(userRepository.findAllById(memberIds));
        if (members.stream().noneMatch(m -> m.getId().equals(creator.getId()))) {
            members.add(creator);
        }

        GroupChat groupChat = GroupChat.builder()
                .name(name)
                .description(description)
                .profilePicture(profilePicture)
                .createdBy(creator)
                .members(members)
                .build();
        groupChat = groupChatRepository.save(groupChat);
        groupChatAdminRepository.save(GroupChatAdmin.builder()
            .groupChat(groupChat).user(creator).build());
        return groupChat;
    }

    public List<GroupChat> getUserGroups() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return groupChatRepository.findByMembersIdOrderByLastMessageAtDesc(currentUser.getId());
    }

    public GroupChat getGroup(Long groupChatId) {
        return groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));
    }

    @Transactional
    public void addMember(Long groupChatId, Long userId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));

        if (!groupChat.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only group creator can add members");
        }

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (groupChat.getMembers().stream().anyMatch(m -> m.getId().equals(userId))) {
            throw new RuntimeException("User is already a member");
        }

        groupChat.getMembers().add(newMember);
        groupChatRepository.save(groupChat);
    }

    @Transactional
    public void removeMember(Long groupChatId, Long userId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));

        if (!groupChat.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only group creator can remove members");
        }

        groupChat.getMembers().removeIf(m -> m.getId().equals(userId));
        groupChatRepository.save(groupChat);
    }

    @Transactional
    public void leaveGroup(Long groupChatId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));

        groupChat.getMembers().removeIf(m -> m.getId().equals(currentUser.getId()));
        groupChatRepository.save(groupChat);
    }

    @Transactional
    public GroupChatMessage sendMessage(Long groupChatId, String content, String messageType,
                                         String mediaUrl, String mediaType, Long replyToId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));

        GroupChatMessage replyTo = replyToId != null ?
                groupChatMessageRepository.findById(replyToId).orElse(null) : null;

        GroupChatMessage msg = GroupChatMessage.builder()
                .groupChat(groupChat)
                .sender(sender)
                .content(content)
                .messageType(messageType != null ? messageType : "TEXT")
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .replyTo(replyTo)
                .build();

        msg = groupChatMessageRepository.save(msg);
        groupChat.setLastMessage(content != null ? content : "[Media]");
        groupChat.setLastMessageAt(msg.getCreatedAt());
        groupChatRepository.save(groupChat);
        return msg;
    }

    public Page<GroupChatMessage> getMessages(Long groupChatId, int page, int size) {
        return groupChatMessageRepository.findByGroupChatId(groupChatId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional
    public void setGroupNickname(Long groupChatId, String nickname) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));

        ChatSetting setting = chatSettingRepository.findByUserIdAndGroupChatId(currentUser.getId(), groupChatId)
                .orElseGet(() -> {
                    ChatSetting s = new ChatSetting();
                    s.setUser(currentUser);
                    s.setGroupChat(groupChat);
                    return s;
                });
        setting.setNickname(nickname);
        chatSettingRepository.save(setting);
    }

    @Transactional
    public void setGroupTheme(Long groupChatId, String theme) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));

        ChatSetting setting = chatSettingRepository.findByUserIdAndGroupChatId(currentUser.getId(), groupChatId)
                .orElseGet(() -> {
                    ChatSetting s = new ChatSetting();
                    s.setUser(currentUser);
                    s.setGroupChat(groupChat);
                    return s;
                });
        setting.setTheme(theme);
        chatSettingRepository.save(setting);
    }

    @Transactional
    public void promoteToAdmin(Long groupChatId, Long userId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));
        if (!groupChat.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only group creator can promote admins");
        }
        if (!groupChatAdminRepository.existsByGroupChatIdAndUserId(groupChatId, userId)) {
            User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            groupChatAdminRepository.save(GroupChatAdmin.builder()
                .groupChat(groupChat).user(target).build());
        }
    }

    @Transactional
    public void demoteAdmin(Long groupChatId, Long userId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));
        if (!groupChat.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only group creator can demote admins");
        }
        groupChatAdminRepository.deleteByGroupChatIdAndUserId(groupChatId, userId);
    }

    public boolean isAdmin(Long groupChatId, Long userId) {
        return groupChatAdminRepository.existsByGroupChatIdAndUserId(groupChatId, userId);
    }

    public List<User> getAdmins(Long groupChatId) {
        return groupChatAdminRepository.findByGroupChatId(groupChatId).stream()
            .map(GroupChatAdmin::getUser).toList();
    }

    @Transactional
    public void updateGroupDescription(Long groupChatId, String description) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));
        if (!groupChat.getCreatedBy().getId().equals(currentUser.getId()) &&
            !groupChatAdminRepository.existsByGroupChatIdAndUserId(groupChatId, currentUser.getId())) {
            throw new RuntimeException("Only admins can update group description");
        }
        groupChat.setDescription(description);
        groupChatRepository.save(groupChat);
    }

    @Transactional
    public void muteGroup(Long groupChatId, Long userId, boolean muted) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ChatSetting setting = chatSettingRepository.findByUserIdAndGroupChatId(userId, groupChatId)
                .orElseGet(() -> {
                    ChatSetting s = new ChatSetting();
                    s.setUser(currentUser);
                    s.setGroupChat(groupChat);
                    return s;
                });
        setting.setMutedNotifications(muted);
        chatSettingRepository.save(setting);
    }

    @Transactional
    public GroupChatMessageReaction reactToGroupMessage(Long messageId, Long userId, String reaction) {
        GroupChatMessage message = groupChatMessageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        groupChatMessageReactionRepository.findByMessageIdAndUserId(messageId, userId)
            .ifPresent(groupChatMessageReactionRepository::delete);
        return groupChatMessageReactionRepository.save(GroupChatMessageReaction.builder()
            .message(message).user(user).reaction(reaction).build());
    }

    public List<GroupChatMessageReaction> getGroupMessageReactions(Long messageId) {
        return groupChatMessageReactionRepository.findByMessageId(messageId);
    }

    @Transactional
    public void removeGroupMessageReaction(Long messageId, Long userId) {
        groupChatMessageReactionRepository.deleteByMessageIdAndUserId(messageId, userId);
    }
}