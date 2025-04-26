package com.example.movies.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;
    
    private String content;
    private double rating;
    
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;
    
    // Reviewer username – review authorship is tracked as a simple String.
    private String reviewer;
    
    public Review() { }

    public Review(Movie movie, String content, double rating, String reviewer) {
        this.movie = movie;
        this.content = content;
        this.rating = rating;
        this.reviewer = reviewer;
        this.status = ReviewStatus.PENDING;
    }
    
    // ...existing getters/setters...
    public Long getId() { return id; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
}
