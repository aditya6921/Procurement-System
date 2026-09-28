package com.procureflow.rfq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RfqSupplierRepository
        extends JpaRepository<RfqSupplier, Long> {

    Optional<RfqSupplier>
    findByRfqIdAndSupplierId(
            Long rfqId,
            Long supplierId
    );

    List<RfqSupplier>
    findByRfqId(
            Long rfqId
    );
}