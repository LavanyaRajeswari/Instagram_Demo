package com.web.Instagram.repository;

import com.web.Instagram.entity.NoteReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteReplyRepository extends JpaRepository<NoteReply, Long> {
    List<NoteReply> findByNoteIdOrderByCreatedAtAsc(Long noteId);
    long countByNoteId(Long noteId);
}