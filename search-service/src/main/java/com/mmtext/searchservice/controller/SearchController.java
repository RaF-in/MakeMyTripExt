package com.mmtext.searchservice.controller;

import com.mmtext.searchservice.esdocument.MovieDocument;
import com.mmtext.searchservice.esdocument.ShowDocument;
import com.mmtext.searchservice.esdocument.TheaterDocument;
import com.mmtext.searchservice.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 🔎 Fuzzy search by movie title
     * GET /api/search/movies?keyword=avengers
     */
    @GetMapping("/movies")
    public ResponseEntity<List<MovieDocument>> searchMovies(
            @RequestParam String keyword) {
        List<MovieDocument> movies = searchService.searchMovies(keyword);
        return ResponseEntity.ok(movies);
    }

    /**
     * 🏠 Theaters near a geo point
     * GET /api/search/theaters/nearby?lat=40.7128&lon=-74.0060&distance=5km
     */
    @GetMapping("/theaters/nearby")
    public ResponseEntity<List<TheaterDocument>> searchTheatersNear(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10km") String distance) {
        List<TheaterDocument> theaters = searchService.searchTheatersNear(lat, lon, distance);
        return ResponseEntity.ok(theaters);
    }

    /**
     * 🎭 Find theaters showing a given movie
     * GET /api/search/theaters/by-movie?movieTitle=Inception
     */
    @GetMapping("/theaters/by-movie")
    public ResponseEntity<List<TheaterDocument>> searchTheatersByMovie(
            @RequestParam String movieTitle) {
        List<TheaterDocument> theaters = searchService.searchTheatersByMovie(movieTitle);
        return ResponseEntity.ok(theaters);
    }

    /**
     * 💰 Filter shows by ticket price and movie
     * GET /api/search/shows?movieTitle=Avatar&minPrice=100&maxPrice=500
     */
    @GetMapping("/shows")
    public ResponseEntity<List<ShowDocument>> searchShows(
            @RequestParam String movieTitle,
            @RequestParam int minPrice,
            @RequestParam int maxPrice) {
        List<ShowDocument> shows = searchService.searchShows(movieTitle, minPrice, maxPrice);
        return ResponseEntity.ok(shows);
    }

    /**
     * 🔮 Autocomplete for movie titles
     * GET /api/search/movies/autocomplete?prefix=ave
     */
    @GetMapping("/movies/autocomplete")
    public ResponseEntity<List<MovieDocument>> autocompleteMovie(
            @RequestParam String prefix) {
        List<MovieDocument> movies = searchService.autocompleteMovie(prefix);
        return ResponseEntity.ok(movies);
    }

    /**
     * 🔍 Advanced movie search with filters and sorting
     * GET /api/search/movies/advanced?keyword=action&language=English&genre=Action&minRating=4.0&sortBy=rating&desc=true
     */
    @GetMapping("/movies/advanced")
    public ResponseEntity<List<MovieDocument>> searchMoviesAdvanced(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "false") boolean desc) {
        List<MovieDocument> movies = searchService.searchMoviesAdvanced(
                keyword, language, genre, minRating, sortBy, desc);
        return ResponseEntity.ok(movies);
    }

    /**
     * 📅 Filter shows within a given time range
     * GET /api/search/shows/by-date?start=2025-11-13T10:00:00Z&end=2025-11-13T22:00:00Z
     */
    @GetMapping("/shows/by-date")
    public ResponseEntity<List<ShowDocument>> searchShowsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        List<ShowDocument> shows = searchService.searchShowsByDateRange(start, end);
        return ResponseEntity.ok(shows);
    }

    /**
     * ⏰ Find shows near user's location starting within next 2 hours
     * GET /api/search/shows/near-now?lat=40.7128&lon=-74.0060&distanceKm=5km
     */
    @GetMapping("/shows/near-now")
    public ResponseEntity<List<ShowDocument>> searchShowsNearNow(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10km") String distanceKm) {
        List<ShowDocument> shows = searchService.searchShowsNearNow(lat, lon, distanceKm);
        return ResponseEntity.ok(shows);
    }

    /**
     * 🔥 Trending movies based on number of shows
     * GET /api/search/movies/trending
     */
    @GetMapping("/movies/trending")
    public ResponseEntity<List<MovieDocument>> trendingMovies() {
        List<MovieDocument> movies = searchService.trendingMovies();
        return ResponseEntity.ok(movies);
    }

    /**
     * ⚖️ Weighted multi-field search (for best ranking)
     * GET /api/search/movies/weighted?text=action thriller
     */
    @GetMapping("/movies/weighted")
    public ResponseEntity<List<MovieDocument>> weightedSearch(
            @RequestParam String text) {
        List<MovieDocument> movies = searchService.weightedSearch(text);
        return ResponseEntity.ok(movies);
    }
}