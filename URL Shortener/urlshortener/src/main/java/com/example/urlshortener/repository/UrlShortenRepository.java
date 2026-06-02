package com.example.urlshortener.repository;

import com.example.urlshortener.entity.UrlMapping;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlShortenRepository extends JpaRepository<UrlMapping, Long> {
    
    Optional<UrlMapping> findByUrlHash(String urlHash);
    Optional<UrlMapping> findByShortCode(String shortCode);

    @Query("SELECT COALESCE(MAX(u.id), 0) FROM UrlMapping u")
    Long findMaxId();
}
