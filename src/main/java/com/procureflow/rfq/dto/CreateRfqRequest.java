package com.procureflow.rfq.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateRfqRequest(

        @Size(
                max = 250
        )
        String title,

        @Size(
                max = 2000
        )
        String description,

        @NotNull(
                message = "Quotation deadline is required"
        )
        @Future(
                message = "Quotation deadline must be in the future"
        )
        LocalDateTime quotationDeadline

) {
}