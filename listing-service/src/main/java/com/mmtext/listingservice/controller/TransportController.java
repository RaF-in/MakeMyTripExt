package com.mmtext.listingservice.controller;

import com.mmtext.listingservice.dto.AirCraftAddOrUpdateRequestDTO;
import com.mmtext.listingservice.dto.BusAddOrUpdateRequestDTO;
import com.mmtext.listingservice.mapper.AirCraftMapper;
import com.mmtext.listingservice.mapper.BusMapper;
import com.mmtext.listingservice.model.AirCraft;
import com.mmtext.listingservice.model.Bus;
import com.mmtext.listingservice.model.Transport;
import com.mmtext.listingservice.service.TransportServiceMmtExt;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/transport")
public class TransportController {
    @Autowired
    TransportServiceMmtExt transportServiceMmtExt;
    @GetMapping
    public ResponseEntity<List<Transport>> getAllTransports() {
        return ResponseEntity.ok().body(transportServiceMmtExt.getAllTransports());
    }
    @GetMapping(path="/airCraft")
    public ResponseEntity<List<AirCraft>> getAllAirCrafts() {
        return ResponseEntity.ok().body(transportServiceMmtExt.getAllAirCrafts());
    }
    @GetMapping(path = "/bus")
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok().body(transportServiceMmtExt.getAllBuses());
    }

    @PostMapping(path="/airCraft")
    public ResponseEntity<AirCraft> addAirCraft(@Valid @RequestBody AirCraftAddOrUpdateRequestDTO request) {
        return ResponseEntity.ok().body(transportServiceMmtExt.addAirCraft(AirCraftMapper.toModel(request)));
    }
    @PostMapping(path="/bus")
    public ResponseEntity<Bus> addBus(@Valid @RequestBody BusAddOrUpdateRequestDTO request) {
        return ResponseEntity.ok().body(transportServiceMmtExt.addBus(BusMapper.toModel(request)));
    }
    @PutMapping("/airCraft")
    public ResponseEntity<AirCraft> updateAirCraft(@Valid @RequestBody AirCraftAddOrUpdateRequestDTO request) {
        return ResponseEntity.ok().body(transportServiceMmtExt.updateAirCraft(AirCraftMapper.toModel(request)));
    }
    @PutMapping("/bus")
    public ResponseEntity<Bus> updateBus(@Valid @RequestBody BusAddOrUpdateRequestDTO request) {
        return ResponseEntity.ok().body(transportServiceMmtExt.updateBus(BusMapper.toModel(request)));
    }
    @DeleteMapping("/airCraft/{id}")
    public ResponseEntity<Boolean> deleteAirCraft(@PathVariable Long id) {
        return ResponseEntity.ok().body(transportServiceMmtExt.deleteAirCraftById(id));
    }
    @DeleteMapping("/bus/{id}")
    public ResponseEntity<Boolean> deleteBus(@PathVariable Long id) {
        return ResponseEntity.ok().body(transportServiceMmtExt.deleteBusById(id));
    }
}
