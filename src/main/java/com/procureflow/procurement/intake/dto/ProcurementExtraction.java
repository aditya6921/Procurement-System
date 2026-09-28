package com.procureflow.procurement.intake.dto;

import com.procureflow.procurement.intake.ProcurementCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProcurementExtraction(

        @NotBlank(
                message = "Item description is required"
        )
        @Size(
                max = 500,
                message = "Item description cannot exceed 500 characters"
        )
        String itemDescription,

        @NotNull(
                message = "Quantity is required"
        )
        @Positive(
                message = "Quantity must be greater than zero"
        )
        Integer quantity,

        @NotNull(
                message = "Budget amount is required"
        )
        @DecimalMin(
                value = "0.01",
                message = "Budget amount must be greater than zero"
        )
        BigDecimal budgetAmount,

        @NotBlank(
                message = "Currency is required"
        )
        @Pattern(
                regexp = "^[A-Z]{3}$",
                message = "Currency must be a 3-letter ISO code"
        )
        String currency,

        @NotNull(
                message = "Deadline is required"
        )
        LocalDate deadline,

        @NotNull(
                message = "Procurement category is required"
        )
        ProcurementCategory category

) {
}