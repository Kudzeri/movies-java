package com.example.movies.controller;

import com.example.movies.model.*;
import com.example.movies.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/admin")
public class SeederController {

    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Constants for seeding
    private static final List<String> GENRE_NAMES = List.of(
        "Action", "Comedy", "Drama", "Fantasy", "Horror", "Mystery",
        "Romance", "Thriller", "Western", "Sci-Fi", "Documentary", "Animation"
    );
    
    private static final List<String> AUTHOR_NAMES = List.of(
        "Steven Spielberg", "Martin Scorsese", "Quentin Tarantino",
        "Christopher Nolan", "James Cameron", "Ridley Scott",
        "Stanley Kubrick", "Peter Jackson", "Alfred Hitchcock", "David Fincher"
    );
    
    @PostMapping("/seed")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String seedData() {
        // Clean existing data if needed (optional)
        reviewRepository.deleteAll();
        movieRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();
        userRepository.deleteAll();

        // Create genres
        List<Genre> genres = new ArrayList<>();
        for(String name : GENRE_NAMES) {
            Genre genre = new Genre(name);
            genres.add(genreRepository.save(genre));
        }
        
        // Create authors
        List<Author> authors = new ArrayList<>();
        for(String name : AUTHOR_NAMES) {
            Author author = new Author(name, "Biography of " + name);
            authors.add(authorRepository.save(author));
        }
        
        // Create movies
        List<Movie> movies = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            Genre genre = genres.get(ThreadLocalRandom.current().nextInt(genres.size()));
            Author author = authors.get(ThreadLocalRandom.current().nextInt(authors.size()));
            Movie movie = new Movie("Movie " + i, "Description for Movie " + i, genre, author);
            movies.add(movieRepository.save(movie));
        }
        
        // Create roles if missing
        Role userRole = roleRepository.findByName("ROLE_USER");
        if(userRole == null) {
            userRole = roleRepository.save(new Role("ROLE_USER"));
        }
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if(adminRole == null) {
            adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
        }
        
        // Create 14 regular users and 3 admins with same password "123123"
        List<User> regularUsers = new ArrayList<>();
        for (int i = 1; i <= 14; i++) {
            User user = new User("user" + i, passwordEncoder.encode("123123"));
            user.setRoles(new HashSet<>(Arrays.asList(userRole)));
            regularUsers.add(userRepository.save(user));
        }
        for (int i = 1; i <= 3; i++) {
            User admin = new User("admin" + i, passwordEncoder.encode("123123"));
            admin.setRoles(new HashSet<>(Arrays.asList(adminRole)));
            userRepository.save(admin);
        }
        
        // Create reviews for each movie (between 0 and 5 reviews)
        for (Movie movie : movies) {
            int reviewsCount = ThreadLocalRandom.current().nextInt(6);
            for (int j = 1; j <= reviewsCount; j++) {
                double rating = ThreadLocalRandom.current().nextDouble(0, 5.01);
                // Choose a random reviewer from regular users
                User reviewerUser = regularUsers.get(ThreadLocalRandom.current().nextInt(regularUsers.size()));
                Review review = new Review(movie, "Review " + j + " for " + movie.getTitle(), rating, reviewerUser.getUsername());
                // Set review as approved
                review.setStatus(ReviewStatus.APPROVED);
                reviewRepository.save(review);
            }
        }
        
        return "Database seeding complete.";
    }
}
