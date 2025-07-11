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

import com.example.movies.model.Author;
import com.example.movies.repository.AuthorRepository;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/authors")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private AuthorRepository authorRepository;

    @Operation(summary = "Получение авторов", description = "Возвращает список всех авторов")
    @GetMapping
    public List<Author> getAll() {
        try {
            return authorRepository.findAll();
        } catch(Exception ex) {
            logger.error("Ошибка при получении списка авторов", ex);
            return List.of();
        }
    }

    @Operation(summary = "Получение автора по ИД", description = "Возвращает автора по переданному ИД")
    @GetMapping("/{id}")
    public Author getById(@PathVariable Long id) {
        try {
            Optional<Author> author = authorRepository.findById(id);
            return author.orElse(null);
        } catch(Exception ex) {
            logger.error("Ошибка при получении автора с ИД {}", id, ex);
            return null;
        }
    }

    @Operation(summary = "Создание автора", description = "Создаёт нового автора")
    @PostMapping
    public Author create(@RequestBody Author author) {
        try {
            return authorRepository.save(author);
        } catch(Exception ex) {
            logger.error("Ошибка при создании автора", ex);
            return null;
        }
    }

    @Operation(summary = "Обновление автора", description = "Обновляет данные автора по ИД")
    @PutMapping("/{id}")
    public Author update(@PathVariable Long id, @RequestBody Author authorDetails) {
        try {
            Author author = authorRepository.findById(id).orElse(null);
            if (author != null) {
                author.setName(authorDetails.getName());
                author.setBiography(authorDetails.getBiography());
                return authorRepository.save(author);
            }
            return null;
        } catch(Exception ex) {
            logger.error("Ошибка при обновлении автора с ИД {}", id, ex);
            return null;
        }
    }

    @Operation(summary = "Удаление автора", description = "Удаляет автора по ИД")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        try {
            authorRepository.deleteById(id);
        } catch(Exception ex) {
            logger.error("Ошибка при удалении автора с ИД {}", id, ex);
        }
    }
}
