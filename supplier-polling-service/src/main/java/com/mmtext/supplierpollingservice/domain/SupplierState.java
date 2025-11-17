package com.mmtext.supplierpollingservice.domain; // Changed package name to 'entities' as is common practice

import com.mmtext.supplierpollingservice.enums.SupplierHealth;
import com.mmtext.supplierpollingservice.enums.SupplierType;
import org.springframework.data.annotation.Id; // Use Spring Data's Id annotation
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table; // Map the class to a table name

import java.time.Instant;

// Specify the table name for clarity in R2DBC
@Table("supplier_state")
public class SupplierState {

    // Use org.springframework.data.annotation.Id
    // R2DBC generally relies on the database to handle auto-generation,
    // you don't typically use @GeneratedValue with strategy in the entity itself
    // the way you do with JPA. The DB handles generation if the type supports it.
    @Id
    @Column("supplier_id") // Use @Column if field name differs from DB column name
    private String supplierId;

    @Column("supplier_type")
    private SupplierType supplierType;

    private String etag;

    @Column("last_polled_at")
    private Instant lastPolledAt;

    @Column("last_modified_at")
    private Instant lastModifiedAt;

    @Column("consecutive_failures")
    private int consecutiveFailures;

    private SupplierHealth health;

    @Column("polling_interval_ms")
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

    public boolean shouldBackoff() {
        return consecutiveFailures >= 3;
    }

    public long getBackoffDelayMs() {
        // Exponential backoff: 2^failures * base delay (5 seconds)
        return Math.min((long) Math.pow(2, consecutiveFailures) * 5000, 300000); // Max 5 min
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
