package com.mmtext.listingservice.controller;

import com.mmtext.listingservice.dto.ConcertRequestDTO;
import com.mmtext.listingservice.mapper.ConcertMapper;
import com.mmtext.listingservice.model.Concert;
import com.mmtext.listingservice.service.ConcertService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/concert")
public class ConcertController {
    @Autowired
    private ConcertService concertService;
    @GetMapping
    public ResponseEntity<List<Concert>> getAllConcerts() {
        return ResponseEntity.ok().body(concertService.getAllConcerts());
    }
    @PostMapping
    public ResponseEntity<Concert> addConcert(@Valid @RequestBody ConcertRequestDTO request) {
        return ResponseEntity.ok().body(concertService.addConcert(ConcertMapper.toModel(request)));
    }
    @PutMapping("{id}")
    public ResponseEntity<Optional<Concert>> updateConcert(@PathVariable Long id, @Valid @RequestBody ConcertRequestDTO request) {
        return ResponseEntity.ok().body(concertService.updateConcert(id, ConcertMapper.toModel(request)));
    }
    @DeleteMapping("{id}")
    public ResponseEntity<Boolean> deleteConcert(@PathVariable Long id) {
        return ResponseEntity.ok().body(concertService.delete(id));
    }
}
