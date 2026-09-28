package com.procureflow.supplier.dto;

import com.procureflow.procurement.intake.ProcurementCategory;
import com.procureflow.supplier.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record SupplierResponse(

        Long id,

        String companyName,

        String contactEmail,

        String contactPerson,

        Set<ProcurementCategory> categories,

        BigDecimal rating,

        Integer riskScore,

        Integer averageDeliveryDays,

        BigDecimal deliveryPerformance,

        ComplianceStatus complianceStatus,

        SupplierStatus supplierStatus,

        boolean approvedSupplier,

        boolean active,

        Instant createdAt

) {

    public static SupplierResponse from(
            Supplier supplier
    ) {

        return new SupplierResponse(
                supplier.getId(),
                supplier.getCompanyName(),
                supplier.getContactEmail(),
                supplier.getContactPerson(),
                supplier.getCategories(),
                supplier.getRating(),
                supplier.getRiskScore(),
                supplier.getAverageDeliveryDays(),
                supplier.getDeliveryPerformance(),
                supplier.getComplianceStatus(),
                supplier.getSupplierStatus(),
                supplier.isApprovedSupplier(),
                supplier.isActive(),
                supplier.getCreatedAt()
        );
    }
}