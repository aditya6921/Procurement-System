package com.procureflow.rfq.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SubmitQuoteRequest(

        @NotNull
        Long supplierId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal quotedAmount,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency,

        @NotNull
        @Positive
        Integer deliveryDays,

        @NotNull
        @Min(0)
        Integer warrantyMonths,

        @Size(max = 100)
        String paymentTerms,

        @Size(max = 2000)
        String notes

) {
}