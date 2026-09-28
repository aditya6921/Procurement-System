package com.procureflow.supplier;

import com.procureflow.procurement.intake.ProcurementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository
        extends JpaRepository<Supplier, Long> {

    Optional<Supplier>
    findByContactEmailIgnoreCase(
            String contactEmail
    );

    boolean existsByContactEmailIgnoreCase(
            String contactEmail
    );

    List<Supplier>
    findAllByActiveTrueOrderByRatingDesc();

    List<Supplier>
    findAllByCategoriesContainingAndActiveTrue(
            ProcurementCategory category
    );
}