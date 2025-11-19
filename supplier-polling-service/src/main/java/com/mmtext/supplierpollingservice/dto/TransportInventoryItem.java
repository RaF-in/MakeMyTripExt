package com.mmtext.supplierpollingservice.dto;

import com.mmtext.supplierpollingservice.enums.SupplierType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class TransportInventoryItem implements InventoryItem {
    private String id;
    private SupplierType type;
    private String origin;
    private String destination;
    private Instant departureTime;
    private BigDecimal price;
    private int seatsAvailable;
    private String supplierRef;
    private Instant updatedAt;
    private Instant arrivalTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SupplierType getType() {
        return type;
    }

    public void setType(SupplierType type) {
        this.type = type;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Instant getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Instant departureTime) {
        this.departureTime = departureTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public void setSeatsAvailable(int seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    public String getSupplierRef() {
        return supplierRef;
    }

    public void setSupplierRef(String supplierRef) {
        this.supplierRef = supplierRef;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @Override
    public String id() {
        return "";
    }

    @Override
    public SupplierType type() {
        return null;
    }

    @Override
    public String origin() {
        return "";
    }

    @Override
    public String destination() {
        return "";
    }

    @Override
    public Instant departureTime() {
        return null;
    }

    @Override
    public BigDecimal price() {
        return null;
    }

    @Override
    public int seatsAvailable() {
        return 0;
    }

    @Override
    public String supplierRef() {
        return "";
    }

    @Override
    public Instant updatedAt() {
        return null;
    }

    @Override
    public Instant arrivalTime() {
        return null;
    }
}
