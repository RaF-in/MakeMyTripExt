package com.mmtext.listingservice.dto;
import jakarta.validation.constraints.NotBlank;

public class BusAddOrUpdateRequestDTO extends TransportAddOrUpdateRequestDTO {
    private String type;
    @NotBlank(message = "Bus number must be unique and is required")
    private String busNum;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBusNum() {
        return busNum;
    }

    public void setBusNum(String busNum) {
        this.busNum = busNum;
    }
}
