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

import com.example.movies.model.Movie;
import com.example.movies.model.Author;
import com.example.movies.repository.MovieRepository;
import com.example.movies.repository.AuthorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private MovieRepository movieRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Operation(summary = "Поиск фильмов с пагинацией", description = "Ищет фильмы по title и description по заданному запросу. Query должен быть не менее 2 символов.")
    @GetMapping("/movies")
    public ResponseEntity<Page<Movie>> searchMovies(
            @RequestParam String query,
            @Parameter(description = "Номер страницы (по умолчанию 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (по умолчанию 10)") @RequestParam(defaultValue = "10") int size) {
        if (query == null || query.trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must be at least 2 characters long");
        }
        Page<Movie> movies = movieRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, PageRequest.of(page, size));
        movies.forEach(movie -> {
            if (movie.getAuthor() != null && movie.getAuthor().getMovies() != null) {
                movie.getAuthor().getMovies().clear(); // avoid recursion by clearing back–reference collection
            }
        });
        return ResponseEntity.ok(movies);
    }
    
    @Operation(summary = "Поиск авторов с пагинацией", description = "Ищет авторов по имени и биографии по заданному запросу. Query должен быть не менее 2 символов.")
    @GetMapping("/authors")
    public ResponseEntity<Page<Author>> searchAuthors(
            @RequestParam String query,
            @Parameter(description = "Номер страницы (по умолчанию 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (по умолчанию 10)") @RequestParam(defaultValue = "10") int size) {
        if (query == null || query.trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must be at least 2 characters long");
        }
        Page<Author> authors = authorRepository.findByNameContainingIgnoreCaseOrBiographyContainingIgnoreCase(query, query, PageRequest.of(page, size));
        authors.forEach(author -> {
            if (author.getMovies() != null) {
                author.getMovies().clear(); // avoid recursion by clearing movies collection
            }
        });
        return ResponseEntity.ok(authors);
    }
}
