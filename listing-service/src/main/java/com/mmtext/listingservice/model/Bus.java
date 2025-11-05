package com.mmtext.listingservice.model;

import jakarta.persistence.Entity;

@Entity
public class Bus extends Transport {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
