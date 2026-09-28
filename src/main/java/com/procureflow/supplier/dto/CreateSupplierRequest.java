package com.procureflow.supplier.dto;

import com.procureflow.procurement.intake.ProcurementCategory;
import com.procureflow.supplier.ComplianceStatus;
import com.procureflow.supplier.SupplierStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record CreateSupplierRequest(

        @NotBlank(
                message = "Company name is required"
        )
        @Size(max = 200)
        String companyName,

        @NotBlank(
                message = "Contact email is required"
        )
        @Email(
                message = "Contact email must be valid"
        )
        @Size(max = 150)
        String contactEmail,

        @Size(max = 100)
        String contactPerson,

        @NotEmpty(
                message = "At least one supplier category is required"
        )
        Set<ProcurementCategory> categories,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("5.0")
        BigDecimal rating,

        @NotNull
        @Min(0)
        @Max(100)
        Integer riskScore,

        @NotNull
        @Positive
        Integer averageDeliveryDays,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal deliveryPerformance,

        @NotNull
        ComplianceStatus complianceStatus,

        @NotNull
        SupplierStatus supplierStatus,

        boolean approvedSupplier,

        boolean active

) {
}