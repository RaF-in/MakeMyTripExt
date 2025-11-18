package com.mmtext.supplierpollingservice.service;

import com.mmtext.supplierpollingservice.enums.SupplierType;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VolatilityAnalyzer {

    private final Map<String, Integer> searchFrequency = new ConcurrentHashMap<>();

    public long calculateOptimalInterval(String supplierId, SupplierType type) {

        boolean isPeakHours = isPeakBookingTime();
        int frequency = searchFrequency.getOrDefault(supplierId, 0);

        // High demand: poll every 30 seconds
        if (isPeakHours || frequency > 100) {
            return 30000L;
        }

        // Medium demand: poll every 2-5 minutes
        if (frequency > 20) {
            return type == SupplierType.AIRLINE ? 120000L : 300000L;
        }

        // Low demand: poll every 10-30 minutes
        return type == SupplierType.AIRLINE ? 600000L : 1800000L;
    }

    public void recordSearch(String supplierId) {
        searchFrequency.merge(supplierId, 1, Integer::sum);
    }

    private boolean isPeakBookingTime() {
        Instant instant = Instant.now();
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());

        DayOfWeek dayOfWeek = zdt.getDayOfWeek();
        int hour = zdt.getHour();

        boolean isBusinessHours =
                dayOfWeek != DayOfWeek.SATURDAY &&
                        dayOfWeek != DayOfWeek.SUNDAY &&
                        hour >= 9 && hour <= 22;

        return isBusinessHours;

    }
}
