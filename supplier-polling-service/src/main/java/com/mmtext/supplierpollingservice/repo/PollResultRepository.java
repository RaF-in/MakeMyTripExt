package com.mmtext.supplierpollingservice.repo;

import com.mmtext.supplierpollingservice.domain.PollResult;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface PollResultRepository extends ReactiveCrudRepository<PollResult, String> {
    Flux<PollResult> findBySupplierId(String supplierId);
    Flux<PollResult> findBySupplierIdAfter(String supplierId, Instant after);
}
