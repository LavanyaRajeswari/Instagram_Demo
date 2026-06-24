package com.web.Instagram.service;

import com.web.Instagram.entity.Call;
import com.web.Instagram.entity.CallParticipant;
import com.web.Instagram.entity.GroupChat;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.CallParticipantRepository;
import com.web.Instagram.repository.CallRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;
    private final CallParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final GroupChatRepository groupChatRepository;

    @Transactional
    public Call initiateCall(Long calleeId, String callType) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User caller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User callee = userRepository.findById(calleeId)
                .orElseThrow(() -> new RuntimeException("Callee not found"));

        Call call = Call.builder()
                .caller(caller)
                .callee(callee)
                .callType(callType)
                .status("CALLING")
                .build();
        return callRepository.save(call);
    }

    @Transactional
    public Call initiateGroupCall(Long groupChatId, String callType) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User caller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));

        User callee = groupChat.getMembers().stream()
                .filter(m -> !m.getId().equals(caller.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No other members"));

        Call call = Call.builder()
                .caller(caller)
                .callee(callee)
                .callType(callType)
                .status("CALLING")
                .groupCall(true)
                .groupChat(groupChat)
                .build();
        return callRepository.save(call);
    }

    @Transactional
    public Call answerCall(Long callId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));
        call.setStatus("ANSWERED");
        call.setStartedAt(LocalDateTime.now());
        return callRepository.save(call);
    }

    @Transactional
    public Call rejectCall(Long callId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));
        call.setStatus("REJECTED");
        return callRepository.save(call);
    }

    @Transactional
    public Call cancelCall(Long callId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));
        call.setStatus("CANCELLED");
        return callRepository.save(call);
    }

    @Transactional
    public Call endCall(Long callId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));
        call.setStatus("ENDED");
        call.setEndedAt(LocalDateTime.now());
        if (call.getStartedAt() != null) {
            long seconds = java.time.Duration.between(call.getStartedAt(), call.getEndedAt()).getSeconds();
            call.setDurationSeconds((int) seconds);
        }
        return callRepository.save(call);
    }

    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Page<Call> getCallHistory(int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return callRepository.findByUserId(currentUser.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Page<Call> getCallHistoryWithUser(Long otherUserId, int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return callRepository.findByUsers(currentUser.getId(), otherUserId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional
    public void toggleRecording(Long callId, boolean recording) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));
        call.setRecording(recording);
        callRepository.save(call);
    }

    @Transactional
    public void addParticipant(Long callId, Long userId) {
        if (!participantRepository.existsByCallIdAndUserId(callId, userId)) {
            Call call = callRepository.findById(callId)
                    .orElseThrow(() -> new RuntimeException("Call not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            participantRepository.save(CallParticipant.builder()
                    .call(call).user(user).build());
        }
    }

    @Transactional
    public void removeParticipant(Long callId, Long userId) {
        participantRepository.deleteByCallIdAndUserId(callId, userId);
    }

    public List<User> getParticipants(Long callId) {
        return participantRepository.findByCallId(callId).stream()
                .map(CallParticipant::getUser)
                .toList();
    }
}