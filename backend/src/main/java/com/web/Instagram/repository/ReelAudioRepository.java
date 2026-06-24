package com.web.Instagram.repository;

import com.web.Instagram.entity.ReelAudio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReelAudioRepository extends JpaRepository<ReelAudio, Long> {
    List<ReelAudio> findByIsTrendingTrueOrderByUsageCountDesc(Pageable pageable);
    Page<ReelAudio> findByTitleContainingIgnoreCase(String query, Pageable pageable);
    @Query("select r from ReelAudio r where r.genre = :genre order by r.usageCount desc")
    List<ReelAudio> findByGenre(@Param("genre") String genre, Pageable pageable);
    List<ReelAudio> findTop10ByOrderByUsageCountDesc();
}