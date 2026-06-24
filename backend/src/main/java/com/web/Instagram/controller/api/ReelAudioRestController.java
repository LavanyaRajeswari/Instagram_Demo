package com.web.Instagram.controller.api;

import com.web.Instagram.entity.ReelAudio;
import com.web.Instagram.service.ReelAudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reel-audio")
@RequiredArgsConstructor
public class ReelAudioRestController {

    private final ReelAudioService reelAudioService;

    @GetMapping("/trending")
    public ResponseEntity<List<ReelAudio>> getTrendingAudio() {
        return ResponseEntity.ok(reelAudioService.getTrendingAudio());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ReelAudio>> searchAudio(@RequestParam String query,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reelAudioService.searchAudio(query, page, size));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<ReelAudio>> getByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(reelAudioService.getAudioByGenre(genre));
    }

    @PostMapping
    public ResponseEntity<ReelAudio> createAudio(@RequestBody ReelAudio audio) {
        return ResponseEntity.ok(reelAudioService.createAudio(audio));
    }
}