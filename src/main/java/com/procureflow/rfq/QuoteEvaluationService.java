package com.procureflow.rfq;

import com.procureflow.audit.AuditActorType;
import com.procureflow.audit.AuditService;
import com.procureflow.exception.ResourceNotFoundException;
import com.procureflow.rfq.dto.QuoteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class QuoteEvaluationService {

    private static final BigDecimal PRICE_WEIGHT =
            new BigDecimal("0.40");

    private static final BigDecimal DELIVERY_WEIGHT =
            new BigDecimal("0.20");

    private static final BigDecimal WARRANTY_WEIGHT =
            new BigDecimal("0.15");

    private static final BigDecimal RATING_WEIGHT =
            new BigDecimal("0.15");

    private static final BigDecimal RISK_WEIGHT =
            new BigDecimal("0.10");

    private final RfqRepository rfqRepository;
    private final SupplierQuoteRepository quoteRepository;
    private final AuditService auditService;

    public QuoteEvaluationService(
            RfqRepository rfqRepository,
            SupplierQuoteRepository quoteRepository,
            AuditService auditService
    ) {
        this.rfqRepository = rfqRepository;
        this.quoteRepository = quoteRepository;
        this.auditService = auditService;
    }

    @Transactional
    public QuoteEvaluationResponse evaluate(
            Long rfqId,
            String email
    ) {

        Rfq rfq =
                rfqRepository
                        .findById(rfqId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "RFQ not found"
                                )
                        );

        if (rfq.getStatus()
                != RfqStatus.CLOSED) {

            throw new IllegalStateException(
                    "RFQ must be closed before evaluation"
            );
        }

        List<SupplierQuote> quotes =
                quoteRepository
                        .findByRfqId(rfqId);

        if (quotes.isEmpty()) {

            throw new IllegalStateException(
                    "Cannot evaluate an RFQ with no quotes"
            );
        }

        BigDecimal minimumPrice =
                quotes.stream()
                        .map(
                                SupplierQuote::getQuotedAmount
                        )
                        .min(BigDecimal::compareTo)
                        .orElseThrow();

        int minimumDelivery =
                quotes.stream()
                        .map(
                                SupplierQuote::getDeliveryDays
                        )
                        .min(Integer::compareTo)
                        .orElseThrow();

        int maximumWarranty =
                quotes.stream()
                        .map(
                                SupplierQuote::getWarrantyMonths
                        )
                        .max(Integer::compareTo)
                        .orElse(0);

        for (SupplierQuote quote : quotes) {

            BigDecimal priceScore =
                    scorePrice(
                            minimumPrice,
                            quote.getQuotedAmount()
                    );

            BigDecimal deliveryScore =
                    scoreLowerIsBetter(
                            minimumDelivery,
                            quote.getDeliveryDays()
                    );

            BigDecimal warrantyScore =
                    maximumWarranty == 0
                            ? BigDecimal.ZERO
                            : percentage(
                            BigDecimal.valueOf(
                                    quote.getWarrantyMonths()
                            ),
                            BigDecimal.valueOf(
                                    maximumWarranty
                            )
                    );

            BigDecimal ratingScore =
                    percentage(
                            quote.getSupplier()
                                    .getRating(),
                            new BigDecimal("5")
                    );

            BigDecimal riskScore =
                    BigDecimal.valueOf(
                            100 - quote
                                    .getSupplier()
                                    .getRiskScore()
                    );

            BigDecimal total =
                    weighted(
                            priceScore,
                            PRICE_WEIGHT
                    )
                            .add(
                                    weighted(
                                            deliveryScore,
                                            DELIVERY_WEIGHT
                                    )
                            )
                            .add(
                                    weighted(
                                            warrantyScore,
                                            WARRANTY_WEIGHT
                                    )
                            )
                            .add(
                                    weighted(
                                            ratingScore,
                                            RATING_WEIGHT
                                    )
                            )
                            .add(
                                    weighted(
                                            riskScore,
                                            RISK_WEIGHT
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            quote.setScores(
                    priceScore,
                    deliveryScore,
                    warrantyScore,
                    ratingScore,
                    riskScore,
                    total
            );
        }

        quoteRepository.saveAll(quotes);

        SupplierQuote recommended =
                quotes.stream()
                        .max(
                                Comparator.comparing(
                                        SupplierQuote::getTotalScore
                                )
                        )
                        .orElseThrow();

        auditService.log(
                rfq.getRequest().getId(),
                AuditActorType.SYSTEM,
                "SYSTEM",
                "QUOTES_EVALUATED",
                "RFQ="
                        + rfqId
                        + ", recommendedQuote="
                        + recommended.getId()
        );

        List<QuoteResponse> responses =
                quotes.stream()
                        .sorted(
                                Comparator.comparing(
                                        SupplierQuote::getTotalScore
                                ).reversed()
                        )
                        .map(QuoteResponse::from)
                        .toList();

        return new QuoteEvaluationResponse(
                rfqId,
                responses,
                recommended.getId()
        );
    }

    private BigDecimal scorePrice(
            BigDecimal minimum,
            BigDecimal actual
    ) {

        return percentage(
                minimum,
                actual
        );
    }

    private BigDecimal scoreLowerIsBetter(
            int minimum,
            int actual
    ) {

        return percentage(
                BigDecimal.valueOf(minimum),
                BigDecimal.valueOf(actual)
        );
    }

    private BigDecimal percentage(
            BigDecimal numerator,
            BigDecimal denominator
    ) {

        return numerator
                .divide(
                        denominator,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        new BigDecimal("100")
                )
                .min(
                        new BigDecimal("100")
                )
                .max(
                        BigDecimal.ZERO
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal weighted(
            BigDecimal score,
            BigDecimal weight
    ) {

        return score.multiply(weight);
    }
}
