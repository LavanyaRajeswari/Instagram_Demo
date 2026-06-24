package com.web.Instagram.controller.api;

import com.web.Instagram.entity.Call;
import com.web.Instagram.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallRestController {

    private final CallService callService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/start/{userId}")
    public ResponseEntity<Call> startCall(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        Call call = callService.initiateCall(userId, body.getOrDefault("callType", "VOICE"));
        Map<String, Object> event = Map.of(
            "type", "CALL_STARTED",
            "callId", call.getId(),
            "callType", call.getCallType(),
            "callerId", call.getCaller().getId(),
            "callerUsername", call.getCaller().getUsername(),
            "callerProfilePicture", call.getCaller().getProfilePicture()
        );
        messagingTemplate.convertAndSend("/queue/call/" + userId, event);
        return ResponseEntity.ok(call);
    }

    @PostMapping("/group/start/{groupId}")
    public ResponseEntity<Call> startGroupCall(@PathVariable Long groupId, @RequestBody Map<String, String> body) {
        Call call = callService.initiateGroupCall(groupId, body.getOrDefault("callType", "VOICE"));
        return ResponseEntity.ok(call);
    }

    @PostMapping("/{callId}/answer")
    public ResponseEntity<Void> answerCall(@PathVariable Long callId) {
        Call call = callService.answerCall(callId);
        Map<String, Object> event = Map.of(
            "type", "CALL_ACCEPTED",
            "callId", call.getId()
        );
        messagingTemplate.convertAndSend("/queue/call/" + call.getCaller().getId(), event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{callId}/reject")
    public ResponseEntity<Void> rejectCall(@PathVariable Long callId) {
        Call call = callService.rejectCall(callId);
        Map<String, Object> event = Map.of(
            "type", "CALL_REJECTED",
            "callId", call.getId()
        );
        messagingTemplate.convertAndSend("/queue/call/" + call.getCaller().getId(), event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{callId}/cancel")
    public ResponseEntity<Void> cancelCall(@PathVariable Long callId) {
        Call call = callService.cancelCall(callId);
        Map<String, Object> event = Map.of(
            "type", "CALL_CANCELLED",
            "callId", call.getId()
        );
        messagingTemplate.convertAndSend("/queue/call/" + call.getCallee().getId(), event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{callId}/end")
    public ResponseEntity<Void> endCall(@PathVariable Long callId) {
        Call call = callService.endCall(callId);
        Long otherUserId = call.getCaller().getId().equals(getCurrentUserId())
            ? call.getCallee().getId()
            : call.getCaller().getId();
        Map<String, Object> event = Map.of(
            "type", "CALL_ENDED",
            "callId", call.getId(),
            "durationSeconds", call.getDurationSeconds()
        );
        messagingTemplate.convertAndSend("/queue/call/" + otherUserId, event);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        String username = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
        return callService.getUserIdByUsername(username);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<Call>> getCallHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(callService.getCallHistory(page, size));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<Page<Call>> getCallHistoryWithUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(callService.getCallHistoryWithUser(userId, page, size));
    }
}