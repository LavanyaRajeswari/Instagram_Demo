package com.web.Instagram.service;

import com.web.Instagram.entity.ReelAudio;
import com.web.Instagram.repository.ReelAudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReelAudioService {

    private final ReelAudioRepository reelAudioRepository;

    public List<ReelAudio> getTrendingAudio() {
        return reelAudioRepository.findTop10ByOrderByUsageCountDesc();
    }

    public Page<ReelAudio> searchAudio(String query, int page, int size) {
        return reelAudioRepository.findByTitleContainingIgnoreCase(query, PageRequest.of(page, size));
    }

    public List<ReelAudio> getAudioByGenre(String genre) {
        return reelAudioRepository.findByGenre(genre, PageRequest.of(0, 20));
    }

    @Transactional
    public ReelAudio createAudio(ReelAudio audio) {
        return reelAudioRepository.save(audio);
    }

    @Transactional
    public void incrementUsage(Long audioId) {
        reelAudioRepository.findById(audioId).ifPresent(audio -> {
            audio.setUsageCount(audio.getUsageCount() + 1);
            reelAudioRepository.save(audio);
        });
    }
}