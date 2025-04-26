package com.example.movies.controller;

import com.example.movies.model.Review;
import com.example.movies.model.Movie;
import com.example.movies.model.ReviewStatus;
import com.example.movies.repository.ReviewRepository;
import com.example.movies.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private MovieRepository movieRepository;

    // Новый DTO для избежания рекурсии в JSON сериализации
    private static class ReviewDto {
        public Long id;
        public Long movieId;
        public String content;
        public double rating;
        public String reviewer;
        public String status;
        
        public ReviewDto() {
        }
    }

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.id = review.getId();
        dto.movieId = review.getMovie().getId();
        dto.content = review.getContent();
        dto.rating = review.getRating();
        dto.reviewer = review.getReviewer();
        dto.status = review.getStatus().name();
        return dto;
    }
    
    @Operation(summary = "Получение отзывов", description = "Возвращает список всех отзывов")
    @GetMapping("/reviews")
    public List<ReviewDto> getAllReviews() {
        try {
            List<Review> reviews = reviewRepository.findAll();
            return reviews.stream().map(this::toDto).toList();
        } catch(Exception ex) {
            logger.error("Ошибка при получении отзывов", ex);
            return List.of();
        }
    }
    
    @Operation(summary = "Получение отзыва по ИД", description = "Возвращает отзыв по указанному ИД")
    @GetMapping("/reviews/{id}")
    public ReviewDto getReviewById(@PathVariable Long id) {
        try {
            Optional<Review> review = reviewRepository.findById(id);
            return review.map(this::toDto).orElse(null);
        } catch(Exception ex) {
            logger.error("Ошибка при получении отзыва с ИД {}", id, ex);
            return null;
        }
    }
    
    @Operation(summary = "Создание отзыва", description = "Создаёт новый отзыв для фильма; доступно всем аутентифицированным пользователям")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reviews")
    public ReviewDto createReview(@RequestParam Long movieId,
                               @RequestParam String content,
                               @RequestParam double rating,
                               @RequestParam String reviewer) {
        try {
            Optional<Movie> movieOpt = movieRepository.findById(movieId);
            if (!movieOpt.isPresent()) {
                logger.warn("Фильм с ИД {} не найден", movieId);
                return null;
            }
            Review newReview = new Review(movieOpt.get(), content, rating, reviewer);
            Review savedReview = reviewRepository.save(newReview);
            return toDto(savedReview);
        } catch(Exception ex) {
            logger.error("Ошибка при создании отзыва для фильма с ИД {}", movieId, ex);
            return null;
        }
    }
    
    @Operation(summary = "Обновление отзыва", description = "Обновляет отзыв и изменяет его статус; действие выполняют админы")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/admin/reviews/{id}")
    public ReviewDto updateReview(@PathVariable Long id,
                               @RequestBody Review reviewDetails) {
        try {
            Optional<Review> reviewOpt = reviewRepository.findById(id);
            if (!reviewOpt.isPresent()) {
                logger.warn("Отзыв с ИД {} не найден", id);
                return null;
            }
            Review review = reviewOpt.get();
            review.setContent(reviewDetails.getContent());
            review.setRating(reviewDetails.getRating());
            review.setStatus(reviewDetails.getStatus());
            Review updatedReview = reviewRepository.save(review);
            return toDto(updatedReview);
        } catch(Exception ex) {
            logger.error("Ошибка при обновлении отзыва с ИД {}", id, ex);
            return null;
        }
    }
    
    // Добавляем новый роут для одобрения отзыва
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/admin/reviews/{id}/approve")
    public ReviewDto approveReview(@PathVariable Long id) {
        try {
            Optional<Review> reviewOpt = reviewRepository.findById(id);
            if (!reviewOpt.isPresent()) {
                logger.warn("Отзыв с ИД {} не найден", id);
                return null;
            }
            Review review = reviewOpt.get();
            review.setStatus(ReviewStatus.APPROVED);
            Review approvedReview = reviewRepository.save(review);
            return toDto(approvedReview);
        } catch(Exception ex) {
            logger.error("Ошибка при одобрении отзыва с ИД {}", id, ex);
            return null;
        }
    }
    
    // Добавляем новый роут для отклонения отзыва
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/admin/reviews/{id}/disapprove")
    public ReviewDto disapproveReview(@PathVariable Long id) {
        try {
            Optional<Review> reviewOpt = reviewRepository.findById(id);
            if (!reviewOpt.isPresent()) {
                logger.warn("Отзыв с ИД {} не найден", id);
                return null;
            }
            Review review = reviewOpt.get();
            review.setStatus(ReviewStatus.REJECTED); // changed from DISAPPROVED to REJECTED
            Review disapprovedReview = reviewRepository.save(review);
            return toDto(disapprovedReview);
        } catch(Exception ex) {
            logger.error("Ошибка при отклонении отзыва с ИД {}", id, ex);
            return null;
        }
    }
    
    @Operation(summary = "Удаление отзыва", description = "Удаляет отзыв; удалять могут пользователи, являющиеся авторами отзыва")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/reviews/{id}")
    public void deleteReview(@PathVariable Long id) {
        try {
            reviewRepository.deleteById(id);
        } catch(Exception ex) {
            logger.error("Ошибка при удалении отзыва с ИД {}", id, ex);
        }
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/reviews/movie/{movieId}")
    public List<ReviewDto> getReviewsByMovieId(@PathVariable Long movieId) {
        try {
            List<Review> reviews = reviewRepository.findByMovieId(movieId);
            return reviews.stream().map(this::toDto).toList();
        } catch(Exception ex) {
            logger.error("Ошибка при получении отзывов для фильма с ИД {}", movieId, ex);
            return List.of();
        }
    }
}
