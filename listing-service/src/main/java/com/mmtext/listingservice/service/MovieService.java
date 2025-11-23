package com.mmtext.listingservice.service;

import com.mmtext.listingservice.dto.HotelResponseDTO;
import com.mmtext.listingservice.model.Hotel;
import com.mmtext.listingservice.model.Movie;
import com.mmtext.listingservice.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {
    @Autowired
    private MovieRepo movieRepo;
    public List<Movie> getAllMovies() {
        return movieRepo.findAll();
    }

    public Movie save(Movie movie) {
        movieRepo.save(movie);
        return movie;
    }

    public Boolean delete(Long id) {
        movieRepo.deleteById(id);
        return !movieRepo.existsById(id);
    }


    public Boolean deleteAll() {
        movieRepo.deleteAll();
        return true;
    }
}
