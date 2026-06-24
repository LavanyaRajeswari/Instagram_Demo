package com.web.Instagram.service;

import com.web.Instagram.dto.note.NoteResponse;
import com.web.Instagram.entity.*;
import com.web.Instagram.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteReplyRepository noteReplyRepository;
    private final NoteLikeRepository noteLikeRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public List<NoteResponse> getActiveNotes(Long currentUserId) {
        User currentUser = getUserOrThrow(currentUserId);
        List<String> visibleAudiences = List.of("PUBLIC", "FOLLOWERS", "MUTUALS");
        List<Note> notes = noteRepository.findByExpiresAtAfterAndAudienceInOrderByCreatedAtDesc(
            LocalDateTime.now(), visibleAudiences);
        return notes.stream()
            .filter(n -> canViewNote(currentUser, n))
            .map(n -> toResponse(n, currentUserId))
            .toList();
    }

    public List<NoteResponse> getUserNotes(Long userId, Long currentUserId) {
        List<String> visibleAudiences = List.of("PUBLIC", "FOLLOWERS", "MUTUALS");
        return noteRepository.findByUserIdAndExpiresAtAfterAndAudienceInOrderByCreatedAtDesc(
            userId, LocalDateTime.now(), visibleAudiences).stream()
            .filter(n -> canViewNote(getUserOrThrow(currentUserId), n))
            .map(n -> toResponse(n, currentUserId))
            .toList();
    }

    @Transactional
    public NoteResponse createNote(Long userId, String text, String color, String audience, Integer expiryHours) {
        User user = getUserOrThrow(userId);
        if (expiryHours == null) expiryHours = 24;
        Note note = Note.builder()
            .user(user).text(text).color(color)
            .audience(audience != null ? audience : "PUBLIC")
            .expiresAt(LocalDateTime.now().plusHours(expiryHours))
            .build();
        return toResponse(noteRepository.save(note), userId);
    }

    @Transactional
    public NoteResponse editNote(Long noteId, String text, String color, String audience, Integer expiryHours) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found"));
        if (text != null) note.setText(text);
        if (color != null) note.setColor(color);
        if (audience != null) note.setAudience(audience);
        if (expiryHours != null) note.setExpiresAt(LocalDateTime.now().plusHours(expiryHours));
        return toResponse(noteRepository.save(note), note.getUser().getId());
    }

    @Transactional
    public void deleteNote(Long noteId) {
        noteLikeRepository.findAll().stream()
            .filter(nl -> nl.getNote().getId().equals(noteId))
            .forEach(nl -> noteLikeRepository.delete(nl));
        noteReplyRepository.findAll().stream()
            .filter(nr -> nr.getNote().getId().equals(noteId))
            .forEach(nr -> noteReplyRepository.delete(nr));
        noteRepository.deleteById(noteId);
    }

    @Transactional
    public NoteReply replyToNote(Long noteId, Long userId, String text) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found"));
        User user = getUserOrThrow(userId);
        NoteReply reply = NoteReply.builder().note(note).user(user).text(text).build();
        return noteReplyRepository.save(reply);
    }

    public List<NoteReply> getNoteReplies(Long noteId) {
        return noteReplyRepository.findByNoteIdOrderByCreatedAtAsc(noteId);
    }

    @Transactional
    public void likeNote(Long noteId, Long userId) {
        if (!noteLikeRepository.existsByNoteIdAndUserId(noteId, userId)) {
            Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
            User user = getUserOrThrow(userId);
            noteLikeRepository.save(NoteLike.builder().note(note).user(user).build());
        }
    }

    @Transactional
    public void unlikeNote(Long noteId, Long userId) {
        noteLikeRepository.deleteByNoteIdAndUserId(noteId, userId);
    }

    private boolean canViewNote(User currentUser, Note note) {
        if (note.getUser().getId().equals(currentUser.getId())) return true;
        String audience = note.getAudience();
        if (audience == null || "PUBLIC".equals(audience)) return true;
        if ("FOLLOWERS".equals(audience)) {
            return followRepository.existsByFollowerIdAndFollowingId(
                currentUser.getId(), note.getUser().getId());
        }
        if ("MUTUALS".equals(audience)) {
            return followRepository.existsByFollowerIdAndFollowingId(
                currentUser.getId(), note.getUser().getId())
                && followRepository.existsByFollowerIdAndFollowingId(
                    note.getUser().getId(), currentUser.getId());
        }
        return false;
    }

    private NoteResponse toResponse(Note note, Long currentUserId) {
        User user = note.getUser();
        return NoteResponse.builder()
            .id(note.getId()).text(note.getText()).color(note.getColor())
            .audience(note.getAudience()).expiresAt(note.getExpiresAt())
            .createdAt(note.getCreatedAt())
            .likeCount(noteLikeRepository.countByNoteId(note.getId()))
            .replyCount(noteReplyRepository.countByNoteId(note.getId()))
            .likedByCurrentUser(noteLikeRepository.existsByNoteIdAndUserId(note.getId(), currentUserId))
            .user(NoteResponse.NoteUser.builder()
                .id(user.getId()).username(user.getUsername())
                .fullName(user.getFullName()).profilePicture(user.getProfilePicture()).build())
            .build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}