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
import com.example.movies.model.Genre;
import com.example.movies.model.Author;
import com.example.movies.repository.MovieRepository;
import com.example.movies.repository.GenreRepository;
import com.example.movies.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/movies")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private AuthorRepository authorRepository;

    @Operation(summary = "Получение фильмов", description = "Возвращает список всех фильмов с информацией о жанрах и авторах")
    @GetMapping
    public List<Movie> getAll() {
        try {
            return movieRepository.findAll();
        } catch(Exception ex) {
            logger.error("Ошибка при получении списка фильмов", ex);
            return List.of();
        }
    }

    @Operation(summary = "Получение фильма по ИД", description = "Возвращает фильм по указанному ИД")
    @GetMapping("/{id}")
    public Movie getById(@PathVariable Long id) {
        try {
            Optional<Movie> movie = movieRepository.findById(id);
            return movie.orElse(null);
        } catch(Exception ex) {
            logger.error("Ошибка при получении фильма с ИД {}", id, ex);
            return null;
        }
    }

    @Operation(summary = "Создание фильма", description = "Создаёт новый фильм с указанием жанра и автора")
    @PostMapping
    public Movie create(@RequestBody Movie movie) {
        try {
            // Fetch related Genre (if provided)
            if (movie.getGenre() != null && movie.getGenre().getId() != null) {
                Optional<Genre> genre = genreRepository.findById(movie.getGenre().getId());
                genre.ifPresent(movie::setGenre);
            }
            // Fetch related Author (if provided)
            if (movie.getAuthor() != null && movie.getAuthor().getId() != null) {
                Optional<Author> author = authorRepository.findById(movie.getAuthor().getId());
                author.ifPresent(movie::setAuthor);
            }
            return movieRepository.save(movie);
        } catch(Exception ex) {
            logger.error("Ошибка при создании фильма", ex);
            return null;
        }
    }

    @Operation(summary = "Обновление фильма", description = "Обновляет данные фильма по ИД")
    @PutMapping("/{id}")
    public Movie update(@PathVariable Long id, @RequestBody Movie movieDetails) {
        try {
            Movie movie = movieRepository.findById(id).orElse(null);
            if (movie != null) {
                movie.setTitle(movieDetails.getTitle());
                movie.setDescription(movieDetails.getDescription());
                if (movieDetails.getGenre() != null && movieDetails.getGenre().getId() != null) {
                    Optional<Genre> genre = genreRepository.findById(movieDetails.getGenre().getId());
                    genre.ifPresent(movie::setGenre);
                }
                if (movieDetails.getAuthor() != null && movieDetails.getAuthor().getId() != null) {
                    Optional<Author> author = authorRepository.findById(movieDetails.getAuthor().getId());
                    author.ifPresent(movie::setAuthor);
                }
                return movieRepository.save(movie);
            }
            return null;
        } catch(Exception ex) {
            logger.error("Ошибка при обновлении фильма с ИД {}", id, ex);
            return null;
        }
    }

    @Operation(summary = "Удаление фильма", description = "Удаляет фильм по указанному ИД")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        try {
            movieRepository.deleteById(id);
        } catch(Exception ex) {
            logger.error("Ошибка при удалении фильма с ИД {}", id, ex);
        }
    }
}
