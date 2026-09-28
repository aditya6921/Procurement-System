package com.procureflow.rfq;

import com.procureflow.supplier.Supplier;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "supplier_quotes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_quote_rfq_supplier",
                        columnNames = {
                                "rfq_id",
                                "supplier_id"
                        }
                )
        }
)
public class SupplierQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quotedAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Integer deliveryDays;

    @Column(nullable = false)
    private Integer warrantyMonths;

    @Column(length = 100)
    private String paymentTerms;

    @Column(length = 2000)
    private String notes;

    private BigDecimal priceScore;
    private BigDecimal deliveryScore;
    private BigDecimal warrantyScore;
    private BigDecimal ratingScore;
    private BigDecimal riskScore;
    private BigDecimal totalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuoteStatus status;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    protected SupplierQuote() {
    }

    public SupplierQuote(
            Rfq rfq,
            Supplier supplier,
            BigDecimal quotedAmount,
            String currency,
            Integer deliveryDays,
            Integer warrantyMonths,
            String paymentTerms,
            String notes
    ) {

        this.rfq = rfq;
        this.supplier = supplier;
        this.quotedAmount = quotedAmount;
        this.currency = currency;
        this.deliveryDays = deliveryDays;
        this.warrantyMonths = warrantyMonths;
        this.paymentTerms = paymentTerms;
        this.notes = notes;
        this.status = QuoteStatus.SUBMITTED;
        this.submittedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Rfq getRfq() {
        return rfq;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public BigDecimal getQuotedAmount() {
        return quotedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public Integer getWarrantyMonths() {
        return warrantyMonths;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getPriceScore() {
        return priceScore;
    }

    public BigDecimal getDeliveryScore() {
        return deliveryScore;
    }

    public BigDecimal getWarrantyScore() {
        return warrantyScore;
    }

    public BigDecimal getRatingScore() {
        return ratingScore;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setScores(
            BigDecimal priceScore,
            BigDecimal deliveryScore,
            BigDecimal warrantyScore,
            BigDecimal ratingScore,
            BigDecimal riskScore,
            BigDecimal totalScore
    ) {

        this.priceScore = priceScore;
        this.deliveryScore = deliveryScore;
        this.warrantyScore = warrantyScore;
        this.ratingScore = ratingScore;
        this.riskScore = riskScore;
        this.totalScore = totalScore;
    }

    public void accept() {
        status = QuoteStatus.ACCEPTED;
    }

    public void reject() {
        status = QuoteStatus.REJECTED;
    }
}