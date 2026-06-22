package com.web.Instagram.service;

import com.web.Instagram.entity.Hashtag;
import com.web.Instagram.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");

    public List<String> extractHashtags(String caption) {
        List<String> tags = new ArrayList<>();
        if (caption == null || caption.isBlank()) return tags;
        Matcher matcher = HASHTAG_PATTERN.matcher(caption);
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }
        return tags;
    }

    @Transactional
    public void saveHashtags(String caption, Long postId) {
        List<String> tags = extractHashtags(caption);
        for (String tag : tags) {
            Hashtag hashtag = Hashtag.builder()
                .tag(tag)
                .postId(postId)
                .build();
            hashtagRepository.save(hashtag);
        }
    }

    @Transactional
    public void removeHashtagsByPost(Long postId) {
        hashtagRepository.deleteByPostId(postId);
    }

    public Page<Long> getPostIdsByTag(String tag, int page, int size) {
        return hashtagRepository.findPostIdsByTag(tag.toLowerCase(), PageRequest.of(page, size));
    }

    public long getPostCountByTag(String tag) {
        return hashtagRepository.countPostsByTag(tag.toLowerCase());
    }

    public List<String> getTrendingHashtags(int limit) {
        Page<Object[]> results = hashtagRepository.findTrendingHashtags(PageRequest.of(0, limit));
        return results.getContent().stream()
            .map(row -> (String) row[0])
            .toList();
    }
}
