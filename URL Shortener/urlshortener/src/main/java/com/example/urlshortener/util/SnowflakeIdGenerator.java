package com.example.urlshortener.util;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.example.urlshortener.repository.UrlShortenRepository;
@Component
public class SnowflakeIdGenerator {

    private final AtomicLong counter;

    public SnowflakeIdGenerator(
            UrlShortenRepository repository) {

        Long maxId = repository.findMaxId();

        this.counter =
                new AtomicLong(maxId + 1);
    }

    public long getNextId() {
        return counter.getAndIncrement();
    }
}
