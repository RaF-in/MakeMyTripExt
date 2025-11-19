package com.mmtext.supplierpollingservice.config;

import com.mmtext.supplierpollingservice.enums.SupplierType;
import org.springframework.stereotype.Component;

@Component
public class PollingConfig {
    private String supplierId;
    private SupplierType supplierType;
    private String baseUrl;
    private String apiKey;
    private Long normalIntervalMs;
    private Long peakIntervalMs;
    private Long lowDemandIntervalMs;
    private int maxRetries;
    private long timeoutMs;
    private boolean supportsEtag;
    private boolean supportsIfModifiedSince;
    private boolean supportsPagination;
    private String healthCheckPath;

    public static PollingConfig airlineDefault(String supplierId, String baseUrl) {
        PollingConfig config = new PollingConfig();
        config.setSupplierId(supplierId);
        config.setBaseUrl(baseUrl);
        config.setSupplierType(SupplierType.AIRLINE);
        config.setNormalIntervalMs(120000L);
        config.setPeakIntervalMs(30000L);   // 30 sec
        config.setLowDemandIntervalMs(600000L); // 10 min
        config.setMaxRetries(3);
        config.setTimeoutMs(10000L);
        config.setSupportsEtag(true);
        config.setSupportsIfModifiedSince(true);
        config.setSupportsPagination(false);
        config.setSupportsEtag(true);
        return config;
    }

    public static PollingConfig hotelDefault(String supplierId, String baseUrl) {
        PollingConfig config = new PollingConfig();
        config.setSupplierId(supplierId);
        config.setSupplierType(SupplierType.HOTEL);
        config.setBaseUrl(baseUrl);
        config.setNormalIntervalMs(300000L);
        config.setPeakIntervalMs(60000L);
        config.setLowDemandIntervalMs(1800000L);
        config.setMaxRetries(3);
        config.setTimeoutMs(15000L);
        config.setSupportsEtag(false);
        config.setSupportsIfModifiedSince(true);
        config.setSupportsPagination(false);
        return config;
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

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Long getNormalIntervalMs() {
        return normalIntervalMs;
    }

    public void setNormalIntervalMs(Long normalIntervalMs) {
        this.normalIntervalMs = normalIntervalMs;
    }

    public Long getPeakIntervalMs() {
        return peakIntervalMs;
    }

    public void setPeakIntervalMs(Long peakIntervalMs) {
        this.peakIntervalMs = peakIntervalMs;
    }

    public Long getLowDemandIntervalMs() {
        return lowDemandIntervalMs;
    }

    public void setLowDemandIntervalMs(Long lowDemandIntervalMs) {
        this.lowDemandIntervalMs = lowDemandIntervalMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isSupportsEtag() {
        return supportsEtag;
    }

    public void setSupportsEtag(boolean supportsEtag) {
        this.supportsEtag = supportsEtag;
    }

    public boolean isSupportsIfModifiedSince() {
        return supportsIfModifiedSince;
    }

    public void setSupportsIfModifiedSince(boolean supportsIfModifiedSince) {
        this.supportsIfModifiedSince = supportsIfModifiedSince;
    }

    public boolean isSupportsPagination() {
        return supportsPagination;
    }

    public void setSupportsPagination(boolean supportsPagination) {
        this.supportsPagination = supportsPagination;
    }

    public String getHealthCheckPath() {
        return healthCheckPath;
    }

    public void setHealthCheckPath(String healthCheckPath) {
        this.healthCheckPath = healthCheckPath;
    }
}
