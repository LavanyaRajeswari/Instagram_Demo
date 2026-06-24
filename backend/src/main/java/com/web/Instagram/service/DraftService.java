package com.web.Instagram.service;

import com.web.Instagram.entity.PostDraft;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.PostDraftRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DraftService {

    private final PostDraftRepository draftRepository;
    private final UserRepository userRepository;

    public List<PostDraft> getUserDrafts(Long userId) {
        return draftRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional
    public PostDraft createDraft(Long userId, String caption, String mediaUrls, String visibility, String location) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        PostDraft draft = PostDraft.builder()
            .user(user).caption(caption).mediaUrls(mediaUrls)
            .visibility(visibility).location(location).build();
        return draftRepository.save(draft);
    }

    @Transactional
    public PostDraft updateDraft(Long draftId, Long userId, String caption, String mediaUrls, String visibility, String location) {
        PostDraft draft = draftRepository.findById(draftId)
            .orElseThrow(() -> new RuntimeException("Draft not found"));
        if (!draft.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        if (caption != null) draft.setCaption(caption);
        if (mediaUrls != null) draft.setMediaUrls(mediaUrls);
        if (visibility != null) draft.setVisibility(visibility);
        if (location != null) draft.setLocation(location);
        return draftRepository.save(draft);
    }

    @Transactional
    public void deleteDraft(Long draftId, Long userId) {
        PostDraft draft = draftRepository.findById(draftId)
            .orElseThrow(() -> new RuntimeException("Draft not found"));
        if (!draft.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        draftRepository.delete(draft);
    }
}