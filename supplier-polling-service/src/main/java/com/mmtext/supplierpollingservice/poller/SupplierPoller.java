package com.mmtext.supplierpollingservice.poller;

import com.mmtext.supplierpollingservice.domain.PollResult;
import com.mmtext.supplierpollingservice.domain.SupplierState;
import reactor.core.publisher.Mono;

public interface SupplierPoller {
    String supplierId();
    Mono<PollResult> poll(SupplierState state);
}
