package com.procureflow.rfq;

import com.procureflow.rfq.dto.QuoteResponse;

import java.util.List;

public record QuoteEvaluationResponse(

        Long rfqId,

        List<QuoteResponse> quotes,

        Long recommendedQuoteId

) {
}