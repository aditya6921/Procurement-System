package com.procureflow.policy;

import com.procureflow.procurement.intake.ProcurementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcurementPolicyRepository
        extends JpaRepository<
        ProcurementPolicy,
        Long> {

    Optional<ProcurementPolicy>
    findFirstByCategoryAndActiveTrueOrderByPriorityAsc(
            ProcurementCategory category
    );

    Optional<ProcurementPolicy>
    findFirstByCategoryIsNullAndActiveTrueOrderByPriorityAsc();
}