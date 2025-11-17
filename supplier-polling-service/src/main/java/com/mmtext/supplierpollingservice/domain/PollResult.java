package com.mmtext.supplierpollingservice.domain;

import com.mmtext.supplierpollingservice.dto.InventoryItem;
import com.mmtext.supplierpollingservice.enums.PollStatus;
import com.mmtext.supplierpollingservice.enums.SupplierType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;

@Table("poll_result")
public class PollResult {

    // Removed @GeneratedValue.
    // R2DBC handles ID generation internally if the database column is set to IDENTITY/SERIAL.
    @Id
    private Long id;

    @Column("supplier_id")
    private String supplierId;

    @Column("supplier_type")
    private SupplierType supplierType;

    // Storing List<InventoryItem> might require a custom converter or JSONB mapping
    // depending on your database setup. R2DBC needs a way to map complex types.
    private List<InventoryItem> items;

    @Column("new_etag")
    private String newEtag;

    @Column("new_cursor")
    private String newCursor;

    @Column("polled_at")
    private Instant polledAt;

    // 'modified' is a boolean, fine as is
    private boolean modified;

    private PollStatus status;

    @Column("error_message")
    private String errorMessage;

    public static PollResult notModified(String supplierId, SupplierType type) {
        PollResult result = new PollResult();
        result.setSupplierId(supplierId);
        result.setSupplierType(type);
        result.setModified(false);
        result.setStatus(PollStatus.NOT_MODIFIED);
        result.setPolledAt(Instant.now());
        return result;
    }

    public static PollResult success(String supplierId, SupplierType type,
                                     List<InventoryItem> items, String etag) {
        PollResult result = new PollResult();
        result.setSupplierId(supplierId);
        result.setSupplierType(type);
        result.setItems(items);
        result.setNewEtag(etag);
        result.setModified(true);
        result.setStatus(PollStatus.SUCCESS);
        result.setPolledAt(Instant.now());

        return result;
    }

    public static PollResult failure(String supplierId, SupplierType type, String error) {
        PollResult result = new PollResult();
        result.setSupplierId(supplierId);
        result.setSupplierType(type);
        result.setStatus(PollStatus.FAILURE);
        result.setErrorMessage(error);
        result.setPolledAt(Instant.now());
        return result;
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

    public List<InventoryItem> getItems() {
        return items;
    }

    public void setItems(List<InventoryItem> items) {
        this.items = items;
    }

    public String getNewEtag() {
        return newEtag;
    }

    public void setNewEtag(String newEtag) {
        this.newEtag = newEtag;
    }

    public String getNewCursor() {
        return newCursor;
    }

    public void setNewCursor(String newCursor) {
        this.newCursor = newCursor;
    }

    public Instant getPolledAt() {
        return polledAt;
    }

    public void setPolledAt(Instant polledAt) {
        this.polledAt = polledAt;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public PollStatus getStatus() {
        return status;
    }

    public void setStatus(PollStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
