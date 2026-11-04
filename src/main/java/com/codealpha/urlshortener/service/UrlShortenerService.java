package com.codealpha.urlshortener.service;

import com.codealpha.urlshortener.model.UrlMapping;
import com.codealpha.urlshortener.repository.UrlMappingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class UrlShortenerService {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_ATTEMPTS = 10;
    private static final int MAX_URL_LENGTH = 2048;

    private final SecureRandom random = new SecureRandom();
    private final UrlMappingRepository repository;

    public UrlShortenerService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    public UrlMapping shorten(String originalUrl) {
        String url = validate(originalUrl);

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generateCode();
            if (repository.existsByShortCode(code)) {
                continue;
            }
            try {
                return repository.save(new UrlMapping(code, url));
            } catch (DataIntegrityViolationException e) {
                // another request took this code first; try a new one
            }
        }
        throw new IllegalStateException("Could not generate a unique short code");
    }

    @Transactional(readOnly = true)
    public Optional<String> findOriginalUrl(String shortCode) {
        return repository.findByShortCode(shortCode).map(UrlMapping::getOriginalUrl);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private String validate(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("URL must not be empty");
        }
        String url = originalUrl.trim();
        if (url.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("URL is too long");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            boolean httpScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            if (!httpScheme || uri.getHost() == null) {
                throw new IllegalArgumentException("URL must start with http:// or https:// and have a valid host");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL is not valid");
        }
        return url;
    }
}