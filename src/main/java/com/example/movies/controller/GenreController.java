package com.example.movies.controller;

import com.example.movies.model.Genre;
import com.example.movies.repository.GenreRepository;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/genres")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class GenreController {

    private static final Logger logger = LoggerFactory.getLogger(GenreController.class);

    @Autowired
    private GenreRepository genreRepository;

    @Operation(summary = "Получение жанров", description = "Возвращает список всех жанров")
    @GetMapping
    public List<Genre> getAll() {
        try {
            return genreRepository.findAll();
        } catch(Exception ex) {
            logger.error("Ошибка при получении списка жанров", ex);
            return List.of();
        }
    }

    @Operation(summary = "Получение жанра по ИД", description = "Возвращает жанр по переданному ИД")
    @GetMapping("/{id}")
    public Genre getById(@PathVariable Long id) {
        try {
            Optional<Genre> genre = genreRepository.findById(id);
            return genre.orElse(null);
        } catch(Exception ex) {
            logger.error("Ошибка при получении жанра с ИД {}", id, ex);
            return null;
        }
    }

    @Operation(summary = "Создание жанра", description = "Создаёт новый жанр")
    @PostMapping
    public Genre create(@RequestBody Genre genre) {
        try {
            return genreRepository.save(genre);
        } catch(Exception ex) {
            logger.error("Ошибка при создании жанра", ex);
            return null;
        }
    }

    @Operation(summary = "Обновление жанра", description = "Обновляет данные жанра по ИД")
    @PutMapping("/{id}")
    public Genre update(@PathVariable Long id, @RequestBody Genre genreDetails) {
        try {
            Genre genre = genreRepository.findById(id).orElse(null);
            if (genre != null) {
                genre.setName(genreDetails.getName());
                return genreRepository.save(genre);
            }
            return null;
        } catch(Exception ex) {
            logger.error("Ошибка при обновлении жанра с ИД {}", id, ex);
            return null;
        }
    }

    @Operation(summary = "Удаление жанра", description = "Удаляет жанр по ИД")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        try {
            genreRepository.deleteById(id);
        } catch(Exception ex) {
            logger.error("Ошибка при удалении жанра с ИД {}", id, ex);
        }
    }
}
