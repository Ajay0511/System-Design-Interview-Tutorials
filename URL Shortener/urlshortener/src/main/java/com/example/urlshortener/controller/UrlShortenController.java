package com.example.urlshortener.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.urlshortener.dto.UrlShortenRequest;
import com.example.urlshortener.dto.UrlShortenResponse;
import com.example.urlshortener.service.UrlShortenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/url")
@RequiredArgsConstructor
public class UrlShortenController {

    private final UrlShortenService service;

    @PostMapping("/shorten")
    public UrlShortenResponse shorten(
            @RequestBody UrlShortenRequest request) {

        String shortCode =
                service.shortenUrl(
                        request.getLongUrl()
                );

        return new UrlShortenResponse(
                "http://localhost:8080/api/url/"
                        + shortCode
        );
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode) {

        String longUrl =
                service.getOriginalUrl(
                        shortCode
                );

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                        URI.create(longUrl)
                )
                .build();
    }
}
