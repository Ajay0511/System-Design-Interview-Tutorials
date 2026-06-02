package com.example.urlshortener.service;

public interface UrlShortenService {
    String shortenUrl(String longUrl);

    String getOriginalUrl(String shortCode);
}
