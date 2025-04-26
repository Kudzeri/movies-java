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
