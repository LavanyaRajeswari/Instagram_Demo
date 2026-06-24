package com.web.Instagram.controller.api;

import com.web.Instagram.dto.note.NoteResponse;
import com.web.Instagram.entity.NoteReply;
import com.web.Instagram.service.NoteService;
import com.web.Instagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteRestController {

    private final NoteService noteService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getActiveNotes(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(noteService.getActiveNotes(userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<NoteResponse>> getMyNotes(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(noteService.getUserNotes(userId, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteResponse>> getUserNotes(Principal principal, @PathVariable Long userId) {
        Long currentUserId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(noteService.getUserNotes(userId, currentUserId));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            Principal principal,
            @RequestParam String text,
            @RequestParam(required = false) String color,
            @RequestParam(required = false, defaultValue = "PUBLIC") String audience,
            @RequestParam(required = false) Integer expiryHours) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(noteService.createNote(userId, text, color, audience, expiryHours));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> editNote(
            @PathVariable Long id,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) Integer expiryHours) {
        return ResponseEntity.ok(noteService.editNote(id, text, color, audience, expiryHours));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{noteId}/reply")
    public ResponseEntity<NoteReply> replyToNote(Principal principal,
                                                  @PathVariable Long noteId,
                                                  @RequestParam String text) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(noteService.replyToNote(noteId, userId, text));
    }

    @GetMapping("/{noteId}/replies")
    public ResponseEntity<List<NoteReply>> getNoteReplies(@PathVariable Long noteId) {
        return ResponseEntity.ok(noteService.getNoteReplies(noteId));
    }

    @PostMapping("/{noteId}/like")
    public ResponseEntity<Void> likeNote(Principal principal, @PathVariable Long noteId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        noteService.likeNote(noteId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{noteId}/like")
    public ResponseEntity<Void> unlikeNote(Principal principal, @PathVariable Long noteId) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        noteService.unlikeNote(noteId, userId);
        return ResponseEntity.ok().build();
    }
}