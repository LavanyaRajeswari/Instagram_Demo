package com.web.Instagram.controller.api;

import com.web.Instagram.dto.note.NoteResponse;
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
    public ResponseEntity<List<NoteResponse>> getActiveNotes() {
        return ResponseEntity.ok(noteService.getActiveNotes());
    }

    @GetMapping("/my")
    public ResponseEntity<List<NoteResponse>> getMyNotes(Principal principal) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(noteService.getUserNotes(userId));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            Principal principal,
            @RequestParam String text,
            @RequestParam(required = false) String color) {
        Long userId = userService.getCurrentUser(principal.getName()).getId();
        return ResponseEntity.ok(noteService.createNote(userId, text, color));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> editNote(
            @PathVariable Long id,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String color) {
        return ResponseEntity.ok(noteService.editNote(id, text, color));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok().build();
    }
}
