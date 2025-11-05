package com.mmtext.listingservice.model;

import jakarta.persistence.Entity;

@Entity
public class AirCraft extends Transport {
    private String aircraftName;
    private String model;

    public String getAircraftName() {
        return aircraftName;
    }

    public void setAircraftName(String aircraftName) {
        this.aircraftName = aircraftName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
