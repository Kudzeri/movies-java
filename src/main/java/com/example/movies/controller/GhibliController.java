package com.example.movies.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ghibli")
@Tag(name = "Ghibli API", description = "Endpoints for interacting with the Studio Ghibli API")
public class GhibliController {

    private final String BASE_URL = "https://ghibli-api.vercel.app/api/films";
    private final String CACHE_NAME = "ghibliFilms";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(summary = "Get all films", description = "Fetches a list of all Studio Ghibli films")
    @GetMapping("/films")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Cacheable(value = CACHE_NAME, key = "'allFilms'")
    public String getAllFilms() {
        try {
            return restTemplate.getForObject(BASE_URL, String.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error fetching films from Ghibli API",
                e
            );
        }
    }

    @Operation(summary = "Get film by ID", description = "Fetches details of a specific Studio Ghibli film by its ID")
    @GetMapping("/films/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Cacheable(value = CACHE_NAME, key = "#id")
    public String getFilmById(@PathVariable String id) {
        try {
            String url = BASE_URL + "/" + id;
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error fetching film from Ghibli API",
                e
            );
        }
    }

    @Operation(summary = "Get cache statistics", description = "Returns information about cached Ghibli films")
    @GetMapping("/cache/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        Map<String, Object> stats = new HashMap<>();
        
        if (cache != null) {
            stats.put("cacheName", CACHE_NAME);
            stats.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
        } else {
            stats.put("error", "Cache not found");
        }
        
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Clear cache", description = "Clears all cached Ghibli films data")
    @DeleteMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        Map<String, String> response = new HashMap<>();
        
        if (cache != null) {
            cache.clear();
            response.put("status", "success");
            response.put("message", "Cache cleared successfully");
        } else {
            response.put("status", "error");
            response.put("message", "Cache not found");
        }
        
        return ResponseEntity.ok(response);
    }
}
