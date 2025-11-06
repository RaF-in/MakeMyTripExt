package com.mmtext.listingservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public class ConcertRequestDTO {
    @NotBlank(message = "Concert title / name is required")
    private String title;
    private OffsetDateTime time;
    @NotBlank(message = "Organizer is required")
    private String organizer;
    @NotBlank(message = "Address is required")
    private AddressRequestDTO addressRequestDTO;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public AddressRequestDTO getAddressRequestDTO() {
        return addressRequestDTO;
    }

    public void setAddressRequestDTO(AddressRequestDTO addressRequestDTO) {
        this.addressRequestDTO = addressRequestDTO;
    }
}
