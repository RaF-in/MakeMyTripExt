package com.mmtext.listingservice.service;

import com.mmtext.listingservice.dto.ConcertRequestDTO;
import com.mmtext.listingservice.exception.ResourceNotFoundException;
import com.mmtext.listingservice.model.Concert;
import com.mmtext.listingservice.repo.ConcertRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConcertService {
    @Autowired
    private ConcertRepo concertRepo;

    public List<Concert> getAllConcerts() {
        return concertRepo.findAll();
    }

    public Concert addConcert(Concert model) {
        return concertRepo.save(model);
    }

    public Optional<Concert> updateConcert(Long id, Concert concert) {
        if (!concertRepo.existsById(id)) {
            throw new ResourceNotFoundException("Concert not found");
        }
        concertRepo.save(concert);
        return concertRepo.findById(id);
    }

    public boolean delete(Long id) {
        if (!concertRepo.existsById(id)) {
            throw new ResourceNotFoundException("Concert not found");
        }
        concertRepo.deleteById(id);
        return !concertRepo.existsById(id);
    }
}
