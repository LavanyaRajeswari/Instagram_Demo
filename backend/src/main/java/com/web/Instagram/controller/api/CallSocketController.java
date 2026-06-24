package com.web.Instagram.controller.api;

import com.web.Instagram.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CallSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallService callService;

    @MessageMapping("/call.offer")
    public void handleOffer(@Payload Map<String, Object> payload) {
        Long calleeId = ((Number) payload.get("calleeId")).longValue();
        messagingTemplate.convertAndSend(
                "/queue/call/" + calleeId,
                payload
        );
    }

    @MessageMapping("/call.answer")
    public void handleAnswer(@Payload Map<String, Object> payload) {
        Long callerId = ((Number) payload.get("callerId")).longValue();
        messagingTemplate.convertAndSend(
                "/queue/call/" + callerId,
                payload
        );
    }

    @MessageMapping("/call.ice-candidate")
    public void handleIceCandidate(@Payload Map<String, Object> payload) {
        Long targetId = ((Number) payload.get("targetId")).longValue();
        messagingTemplate.convertAndSend(
                "/queue/call/" + targetId,
                payload
        );
    }

    @MessageMapping("/call.end")
    public void handleEnd(@Payload Map<String, Object> payload) {
        Long callId = ((Number) payload.get("callId")).longValue();
        callService.endCall(callId);
        Long targetId = ((Number) payload.get("targetId")).longValue();
        messagingTemplate.convertAndSend(
                "/queue/call/" + targetId,
                payload
        );
    }
}