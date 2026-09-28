package com.procureflow.supplier;

import com.procureflow.exception.ConflictException;
import com.procureflow.exception.ResourceNotFoundException;
import com.procureflow.procurement.intake.ProcurementCategory;
import com.procureflow.supplier.dto.CreateSupplierRequest;
import com.procureflow.supplier.dto.UpdateSupplierRequest;
import com.procureflow.supplier.dto.SupplierResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(
            SupplierRepository supplierRepository
    ) {
        this.supplierRepository =
                supplierRepository;
    }

    @Transactional
    public SupplierResponse create(
            CreateSupplierRequest request
    ) {

        String email =
                normalizeEmail(
                        request.contactEmail()
                );

        if (supplierRepository
                .existsByContactEmailIgnoreCase(email)) {

            throw new ConflictException(
                    "A supplier with this contact email already exists"
            );
        }

        Supplier supplier =
                new Supplier(
                        request.companyName().trim(),
                        email,
                        normalize(
                                request.contactPerson()
                        ),
                        request.categories(),
                        request.rating(),
                        request.riskScore(),
                        request.averageDeliveryDays(),
                        request.deliveryPerformance(),
                        request.complianceStatus(),
                        request.supplierStatus(),
                        request.approvedSupplier()
                );

        supplier.setActive(
                request.active()
        );

        Supplier saved =
                supplierRepository.save(
                        supplier
                );

        return SupplierResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getById(
            Long id
    ) {

        return SupplierResponse.from(
                findSupplier(id)
        );
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll() {

        return supplierRepository
                .findAllByActiveTrueOrderByRatingDesc()
                .stream()
                .map(SupplierResponse::from)
                .toList();
    }

    @Transactional
    public SupplierResponse update(
            Long id,
            UpdateSupplierRequest request
    ) {

        Supplier supplier =
                findSupplier(id);

        String email =
                normalizeEmail(
                        request.contactEmail()
                );

        supplierRepository
                .findByContactEmailIgnoreCase(email)
                .ifPresent(existing -> {

                    if (!existing.getId()
                            .equals(id)) {

                        throw new ConflictException(
                                "Another supplier already uses this email"
                        );
                    }
                });

        supplier.update(
                request.companyName().trim(),
                email,
                normalize(
                        request.contactPerson()
                ),
                request.categories(),
                request.rating(),
                request.riskScore(),
                request.averageDeliveryDays(),
                request.deliveryPerformance(),
                request.complianceStatus(),
                request.supplierStatus(),
                request.approvedSupplier(),
                request.active()
        );

        return SupplierResponse.from(
                supplierRepository.save(supplier)
        );
    }

    @Transactional
    public void deactivate(
            Long id
    ) {

        Supplier supplier =
                findSupplier(id);

        supplier.setActive(false);
        supplier.setSupplierStatus(
                SupplierStatus.SUSPENDED
        );

        supplierRepository.save(
                supplier
        );
    }

    @Transactional(readOnly = true)
    public List<Supplier> findEligibleSuppliers(
            ProcurementCategory category,
            int maximumRiskScore,
            java.math.BigDecimal minimumRating,
            boolean requireApprovedSupplier,
            boolean requireCompliantSupplier
    ) {

        return supplierRepository
                .findAllByCategoriesContainingAndActiveTrue(
                        category
                )
                .stream()
                .filter(supplier ->
                        supplier.getSupplierStatus()
                                == SupplierStatus.ACTIVE
                )
                .filter(supplier ->
                        supplier.getRiskScore()
                                <= maximumRiskScore
                )
                .filter(supplier ->
                        supplier.getRating()
                                .compareTo(minimumRating)
                                >= 0
                )
                .filter(supplier ->
                        !requireApprovedSupplier
                                || supplier.isApprovedSupplier()
                )
                .filter(supplier ->
                        !requireCompliantSupplier
                                || supplier.getComplianceStatus()
                                == ComplianceStatus.COMPLIANT
                )
                .toList();
    }

    private Supplier findSupplier(Long id) {

        return supplierRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: " + id
                        )
                );
    }

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase();
    }

    private String normalize(
            String value
    ) {

        if (value == null) {
            return null;
        }

        return value.trim();
    }
}