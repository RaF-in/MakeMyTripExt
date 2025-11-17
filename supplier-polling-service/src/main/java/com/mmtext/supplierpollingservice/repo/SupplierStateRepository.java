package com.mmtext.supplierpollingservice.repo;

import com.mmtext.supplierpollingservice.domain.SupplierState;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface SupplierStateRepository extends ReactiveCrudRepository<SupplierState, String> {

}
