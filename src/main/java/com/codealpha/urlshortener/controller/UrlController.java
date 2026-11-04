package com.codealpha.urlshortener.controller;

import com.codealpha.urlshortener.model.UrlMapping;
import com.codealpha.urlshortener.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class UrlController {

    public record ShortenRequest(String url) {
    }

    public record ShortenResponse(String shortCode, String shortUrl, String originalUrl) {
    }

    private final UrlShortenerService service;

    public UrlController(UrlShortenerService service) {
        this.service = service;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@RequestBody ShortenRequest request) {
        UrlMapping mapping = service.shorten(request.url());

        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{code}")
                .buildAndExpand(mapping.getShortCode())
                .toUriString();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ShortenResponse(mapping.getShortCode(), shortUrl, mapping.getOriginalUrl()));
    }

    // The regex limits this route to 7-character codes, so it won't swallow
    // /index.html, /h2-console or other static files later.
    @GetMapping("/{shortCode:[0-9A-Za-z]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable("shortCode") String shortCode) {
        return service.findOriginalUrl(shortCode)
                .map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(url))
                        .<Void>build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found"));
    }
}