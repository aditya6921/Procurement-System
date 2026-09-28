package com.procureflow.procurement.intake;

import com.procureflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcurementRequestRepository
        extends JpaRepository<ProcurementRequest, Long> {

    List<ProcurementRequest>
    findByRequesterOrderByCreatedAtDesc(
            User requester
    );

    List<ProcurementRequest> findAllByOrderByCreatedAtDesc();
}
