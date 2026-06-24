package com.web.Instagram.repository;

import com.web.Instagram.entity.Hashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findFirstByTag(String tag);
    List<Hashtag> findByPostId(Long postId);

    void deleteByPostId(Long postId);

    @Query("select distinct h.tag from Hashtag h where lower(h.tag) like lower(concat(:query, '%')) order by h.tag")
    List<String> searchHashtags(@Param("query") String query);

    @Query("select h.tag, count(h) as cnt from Hashtag h group by h.tag order by cnt desc")
    Page<Object[]> findTrendingHashtags(Pageable pageable);

    @Query("select distinct h.postId from Hashtag h where h.tag = :tag order by h.createdAt desc")
    Page<Long> findPostIdsByTag(@Param("tag") String tag, Pageable pageable);

    @Query("select count(distinct h.postId) from Hashtag h where h.tag = :tag")
    long countPostsByTag(@Param("tag") String tag);
}