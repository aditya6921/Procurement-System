package com.procureflow.rfq.dto;

import com.procureflow.rfq.Rfq;
import com.procureflow.rfq.RfqStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record RfqResponse(

        Long id,

        Long requestId,

        String title,

        String description,

        LocalDateTime quotationDeadline,

        RfqStatus status,

        Instant createdAt

) {

    public static RfqResponse from(
            Rfq rfq
    ) {

        return new RfqResponse(
                rfq.getId(),
                rfq.getRequest().getId(),
                rfq.getTitle(),
                rfq.getDescription(),
                rfq.getQuotationDeadline(),
                rfq.getStatus(),
                rfq.getCreatedAt()
        );
    }
}