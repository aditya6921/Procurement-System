package com.procureflow.rfq.dto;

import com.procureflow.rfq.*;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteResponse(

        Long id,

        Long rfqId,

        Long supplierId,

        String supplierName,

        BigDecimal quotedAmount,

        String currency,

        Integer deliveryDays,

        Integer warrantyMonths,

        String paymentTerms,

        String notes,

        BigDecimal priceScore,

        BigDecimal deliveryScore,

        BigDecimal warrantyScore,

        BigDecimal ratingScore,

        BigDecimal riskScore,

        BigDecimal totalScore,

        QuoteStatus status,

        Instant submittedAt

) {

    public static QuoteResponse from(
            SupplierQuote quote
    ) {

        return new QuoteResponse(
                quote.getId(),
                quote.getRfq().getId(),
                quote.getSupplier().getId(),
                quote.getSupplier().getCompanyName(),
                quote.getQuotedAmount(),
                quote.getCurrency(),
                quote.getDeliveryDays(),
                quote.getWarrantyMonths(),
                quote.getPaymentTerms(),
                quote.getNotes(),
                quote.getPriceScore(),
                quote.getDeliveryScore(),
                quote.getWarrantyScore(),
                quote.getRatingScore(),
                quote.getRiskScore(),
                quote.getTotalScore(),
                quote.getStatus(),
                quote.getSubmittedAt()
        );
    }
}