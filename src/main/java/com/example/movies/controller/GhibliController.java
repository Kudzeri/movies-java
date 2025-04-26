/*
 * MIT License
 *
 * Copyright (c) 2025 Kudzeri
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
