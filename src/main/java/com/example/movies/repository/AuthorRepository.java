package com.example.movies.repository;

import com.example.movies.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByNameContainingIgnoreCaseOrBiographyContainingIgnoreCase(String name, String biography);
    Page<Author> findByNameContainingIgnoreCaseOrBiographyContainingIgnoreCase(String name, String biography, Pageable pageable);
}
