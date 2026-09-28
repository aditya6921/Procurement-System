package com.procureflow.rfq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RfqRepository
        extends JpaRepository<Rfq, Long> {

    List<Rfq>
    findByRequestIdOrderByCreatedAtDesc(
            Long requestId
    );

    long countByStatus(
            RfqStatus status
    );
}