package com.web.Instagram.service;

import com.web.Instagram.dto.note.NoteResponse;
import com.web.Instagram.entity.Note;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.NoteRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public List<NoteResponse> getActiveNotes() {
        return noteRepository.findByExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NoteResponse> getUserNotes(Long userId) {
        return noteRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(userId, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NoteResponse createNote(Long userId, String text, String color) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Note note = Note.builder()
                .user(user)
                .text(text)
                .color(color)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        return toResponse(noteRepository.save(note));
    }

    @Transactional
    public NoteResponse editNote(Long noteId, String text, String color) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (text != null) note.setText(text);
        if (color != null) note.setColor(color);

        return toResponse(noteRepository.save(note));
    }

    private NoteResponse toResponse(Note note) {
        User user = note.getUser();
        return NoteResponse.builder()
                .id(note.getId())
                .text(note.getText())
                .color(note.getColor())
                .createdAt(note.getCreatedAt())
                .user(NoteResponse.NoteUser.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .profilePicture(user.getProfilePicture())
                        .build())
                .build();
    }

    @Transactional
    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }
}
