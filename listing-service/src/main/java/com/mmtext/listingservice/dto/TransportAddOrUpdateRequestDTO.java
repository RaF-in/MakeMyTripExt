package com.mmtext.listingservice.dto;

import com.mmtext.listingservice.model.TransportSchedule;

import java.util.List;

public class TransportAddOrUpdateRequestDTO {
    private String description;
    private List<TransportSchedule> schedules;
}
