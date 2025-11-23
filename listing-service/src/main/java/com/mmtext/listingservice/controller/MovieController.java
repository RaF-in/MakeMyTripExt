package com.mmtext.listingservice.controller;
import com.mmtext.listingservice.model.Movie;
import com.mmtext.listingservice.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/movie")
public class MovieController {
    @Autowired
    MovieService movieService;
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok().body(movieService.getAllMovies());
    }
    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        return ResponseEntity.ok().body(movieService.save(movie));
    }
    @PutMapping
    public ResponseEntity<Movie> updateMovie(@RequestBody Movie movie) {
        return ResponseEntity.ok().body(movieService.save(movie));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteMovie(@PathVariable Long id) {
        return ResponseEntity.ok().body(movieService.delete(id));
    }
}
