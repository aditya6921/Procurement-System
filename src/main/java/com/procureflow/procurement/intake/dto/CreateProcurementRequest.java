package com.procureflow.procurement.intake.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProcurementRequest(

        @NotBlank(message = "Procurement request is required")

        @Size(
                min = 5,
                max = 4000,
                message =
                        "Procurement request must be between 5 and 4000 characters"
        )

        String request

) {
}