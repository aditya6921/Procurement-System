package com.procureflow.rfq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierQuoteRepository
        extends JpaRepository<
        SupplierQuote,
        Long> {

    boolean existsByRfqIdAndSupplierId(
            Long rfqId,
            Long supplierId
    );

    List<SupplierQuote>
    findByRfqIdOrderByTotalScoreDesc(
            Long rfqId
    );

    List<SupplierQuote>
    findByRfqId(
            Long rfqId
    );
}