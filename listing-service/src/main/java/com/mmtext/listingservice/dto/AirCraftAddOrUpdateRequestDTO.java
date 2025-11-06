package com.mmtext.listingservice.dto;

import com.mmtext.listingservice.model.TransportSchedule;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class AirCraftAddOrUpdateRequestDTO extends TransportAddOrUpdateRequestDTO {
    private String aircraftName;
    private String model;
    @NotBlank(message = "AirCraft code can't be null and is required")
    private String code;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
