package com.example.urlshortener.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.urlshortener.entity.UrlMapping;
import com.example.urlshortener.repository.UrlShortenRepository;
import com.example.urlshortener.util.Base62Encoder;
import com.example.urlshortener.util.HashUtil;
import com.example.urlshortener.util.SnowflakeIdGenerator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class UrlShortenServiceImpl implements UrlShortenService {
    private final UrlShortenRepository urlShortenRepository;

    private final SnowflakeIdGenerator snkowFlakeIdGenerator;

    private final RedisTemplate<String, String> redisTemplate; 

    @Override
    public String shortenUrl(String longUrl){
        String hash = HashUtil.md5digest(longUrl);

        String cachedShortCode = redisTemplate.opsForValue().get("HASH:" + hash);
        if(cachedShortCode != null){
            System.out.println("Data from redis " + cachedShortCode);
            return cachedShortCode;
        }


        Optional<UrlMapping> existing = urlShortenRepository.findByUrlHash(hash);
        if(existing.isPresent()){
            System.out.println("setting hash to short to redis " + existing);

            redisTemplate.opsForValue().set(
                "HASH:" + hash, existing.get().getShortCode(),
                Duration.ofMinutes(30)
            );

            return existing.get().getShortCode();
        }

        Long id = snkowFlakeIdGenerator.getNextId();


        String shortenCode = Base62Encoder.encode(id);

        UrlMapping urlMapping = UrlMapping.builder()
                                .id(id)
                                .shortCode(shortenCode)
                                .urlHash(hash)
                                .longUrl(longUrl)
                                .createdAt(LocalDateTime.now())
                                .build();

        redisTemplate.opsForValue().set(
            "HASH:"+ hash, 
            shortenCode, 
            Duration.ofMinutes(30)
        );
        redisTemplate.opsForValue().set(
            "SHORT:" + shortenCode,
            longUrl,
            Duration.ofMinutes(30)
        );
        urlShortenRepository.save(urlMapping);
        return shortenCode;
    }

    @Override
    public String getOriginalUrl(String shortCode){
        String cachedUrl = redisTemplate.opsForValue().get("SHORT:" + shortCode);
        if(cachedUrl != null){
            return cachedUrl;
        }
        UrlMapping urlMapping =  urlShortenRepository.findByShortCode(shortCode)
         .orElseThrow(() -> new RuntimeException("URL not found"));
        
        redisTemplate.opsForValue().set(
            "SHORT:" + shortCode,
            urlMapping.getLongUrl(),
            Duration.ofMinutes(30)
        );
        return urlMapping.getLongUrl();
    }


}
