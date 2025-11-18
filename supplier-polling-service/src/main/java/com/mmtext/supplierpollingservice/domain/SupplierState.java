package com.mmtext.supplierpollingservice.domain; // Changed package name to 'entities' as is common practice

import com.mmtext.supplierpollingservice.enums.SupplierHealth;
import com.mmtext.supplierpollingservice.enums.SupplierType;

import java.time.Instant;


// Specify the table name for clarity in R2DBC

public class SupplierState {
    private String supplierId;
    private SupplierType supplierType;

    private String etag;
    private Instant lastPolledAt;
    private Instant lastModifiedAt;
    private int consecutiveFailures;

    private SupplierHealth health;
    private Long pollingIntervalMs;

    private String cursor; // For pagination/incremental sync

    public void markSuccess() {
        this.consecutiveFailures = 0;
        this.health = SupplierHealth.HEALTHY;
        this.lastPolledAt = Instant.now();
    }

    public void markFailure() {
        this.consecutiveFailures++;
        this.lastPolledAt = Instant.now();

        if (consecutiveFailures >= 3) {
            this.health = SupplierHealth.DEGRADED;
        }
        if (consecutiveFailures >= 5) {
            this.health = SupplierHealth.UNHEALTHY;
        }
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public SupplierType getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(SupplierType supplierType) {
        this.supplierType = supplierType;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Instant getLastPolledAt() {
        return lastPolledAt;
    }

    public void setLastPolledAt(Instant lastPolledAt) {
        this.lastPolledAt = lastPolledAt;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }

    public SupplierHealth getHealth() {
        return health;
    }

    public void setHealth(SupplierHealth health) {
        this.health = health;
    }

    public Long getPollingIntervalMs() {
        return pollingIntervalMs;
    }

    public void setPollingIntervalMs(Long pollingIntervalMs) {
        this.pollingIntervalMs = pollingIntervalMs;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
