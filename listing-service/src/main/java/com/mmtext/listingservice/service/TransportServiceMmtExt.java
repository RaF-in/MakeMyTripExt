package com.mmtext.listingservice.service;

import com.mmtext.listingservice.exception.AirCraftAlreadyExistException;
import com.mmtext.listingservice.exception.BusAlreadyExistException;
import com.mmtext.listingservice.exception.ResourceNotFoundException;
import com.mmtext.listingservice.model.AirCraft;
import com.mmtext.listingservice.model.Bus;
import com.mmtext.listingservice.model.Transport;
import com.mmtext.listingservice.repo.TransportRepoMmtExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportServiceMmtExt {

    @Autowired
    TransportRepoMmtExt transportRepoMmtExt;

    public List<Transport> getAllTransports() {
        return transportRepoMmtExt.findAll();
    }

    public List<AirCraft> getAllAirCrafts() {
        return transportRepoMmtExt.getAllAirCrafts();
    }

    public List<Bus> getAllBuses() {
        return transportRepoMmtExt.getAllAirBuses();
    }

    public AirCraft addAirCraft(AirCraft airCraft) {
        if (transportRepoMmtExt.findByAircraftCode(airCraft.getCode()) != null) {
            throw new AirCraftAlreadyExistException("AirCraft already exist with code"
            + airCraft.getCode());
        }
        transportRepoMmtExt.save(airCraft);
        return airCraft;
    }

    public Bus addBus(Bus bus) {
        if (transportRepoMmtExt.findByBusNum(bus.getBusNum()) != null) {
            throw new BusAlreadyExistException("Bus already exist with bus number"
                    + bus.getBusNum());
        }
        transportRepoMmtExt.save(bus);
        return bus;
    }

    public AirCraft updateAirCraft(AirCraft airCraft) {
        if (!transportRepoMmtExt.existsById(airCraft.getId())) {
            throw new ResourceNotFoundException("Aircraft not exist");
        }
        if (transportRepoMmtExt.existsByAircraftCodeAndNotById(airCraft.getCode(), airCraft.getId())) {
            throw new AirCraftAlreadyExistException("Can't update  aircraft with duplicate code");
        }
        transportRepoMmtExt.save(airCraft);
        return airCraft;
    }

    public Bus updateBus(Bus bus) {
        if (!transportRepoMmtExt.existsById(bus.getId())) {
            throw new ResourceNotFoundException("Bus not exist");
        }
        if (transportRepoMmtExt.existsByBusNumAndNotById(bus.getBusNum(), bus.getId())) {
            throw new BusAlreadyExistException("Can't update  bus with duplicate bus number");
        }
        transportRepoMmtExt.save(bus);
        return bus;
    }

    public boolean deleteAirCraftById(Long id) {
        if (!transportRepoMmtExt.existsById(id)) {
            throw new ResourceNotFoundException("AirCraft not exist");
        }
        return deleteTransportById(id);
    }

    public boolean deleteBusById(Long id) {
        if (!transportRepoMmtExt.existsById(id)) {
            throw new ResourceNotFoundException("Bus not exist");
        }
        return deleteTransportById(id);
    }

    private boolean deleteTransportById(Long id) {
        transportRepoMmtExt.deleteById(id);
        return !transportRepoMmtExt.existsById(id);
    }
}
