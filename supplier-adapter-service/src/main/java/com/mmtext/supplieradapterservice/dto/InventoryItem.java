package com.mmtext.supplieradapterservice.dto;

import com.mmtext.supplieradapterservice.enums.SupplierType;

import java.math.BigDecimal;
import java.time.Instant;

public interface InventoryItem {
    String id();
    SupplierType type();
    String origin();
    String destination();
    Instant departureTime();
    BigDecimal price();
    int seatsAvailable();
    String supplierRef();
    Instant updatedAt();
    Instant arrivalTime();
}
