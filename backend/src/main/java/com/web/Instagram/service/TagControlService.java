package com.web.Instagram.service;

import com.web.Instagram.entity.TagControl;
import com.web.Instagram.entity.User;
import com.web.Instagram.repository.TagControlRepository;
import com.web.Instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagControlService {

    private final TagControlRepository tagControlRepository;
    private final UserRepository userRepository;

    @Transactional
    public TagControl setTagControl(Long userId, Long targetUserId, String allowTagging, String allowMention) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        User target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new RuntimeException("Target user not found"));
        TagControl tc = tagControlRepository.findByUserIdAndTargetUserId(userId, targetUserId)
            .orElse(TagControl.builder().user(user).targetUser(target).build());
        if (allowTagging != null) tc.setAllowTagging(allowTagging);
        if (allowMention != null) tc.setAllowMention(allowMention);
        return tagControlRepository.save(tc);
    }

    public TagControl getTagControl(Long userId, Long targetUserId) {
        return tagControlRepository.findByUserIdAndTargetUserId(userId, targetUserId).orElse(null);
    }
}