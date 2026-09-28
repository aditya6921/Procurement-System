package com.procureflow.procurement.intake.dto;

import com.procureflow.procurement.intake.ProcurementRequest;

import java.time.Instant;
import java.util.List;

public record ProcurementIntakeResponse(

        Long id,

        String status,

        String rawRequest,

        ProcurementExtraction extraction,

        String message,

        List<String> missingFields,

        Instant createdAt

) {

    public static ProcurementIntakeResponse
    from(
            ProcurementRequest request,
            ProcurementExtraction extraction,
            String message,
            List<String> missingFields
    ) {

        return new ProcurementIntakeResponse(
                request.getId(),
                request.getStatus().name(),
                request.getRawRequest(),
                extraction,
                message,
                missingFields,
                request.getCreatedAt()
        );
    }
}