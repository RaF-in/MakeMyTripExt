package com.mmtext.listingservice.dto;

import jakarta.validation.constraints.NotBlank;

public class AddressRequestDTO {
    private String street;
    @NotBlank(message = "City is required")
    private String city;
    private String state;
    private String zipcode;
    @NotBlank(message = "Country is required")
    private String country;
    @NotBlank(message = "Phone is required")
    private String phone;
    private String email;
}
