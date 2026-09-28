package com.procureflow.procurement.intake;

import com.procureflow.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.procureflow.procurement.intake.dto.ProcurementExtraction;

@Entity
@Table(name = "procurement_requests")
public class ProcurementRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "requester_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_procurement_request_requester"
            )
    )
    private User requester;

    @Column(
            name = "raw_request",
            nullable = false,
            length = 4000
    )
    private String rawRequest;

    @Column(
            name = "item_description",
            length = 500
    )
    private String itemDescription;

    @Column
    private Integer quantity;

    @Column(
            precision = 15,
            scale = 2
    )
    private BigDecimal budgetAmount;

    @Column(length = 3)
    private String currency;

    @Column
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ProcurementCategory category;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private ProcurementRequestStatus status;

    @Column(
            length = 1000
    )
    private String validationMessage;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ProcurementRequest() {
    }

    public ProcurementRequest(
            User requester,
            String rawRequest
    ) {
        this.requester = requester;
        this.rawRequest = rawRequest;
        this.status =
                ProcurementRequestStatus.AI_PROCESSING;
    }

    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getRequester() {
        return requester;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public ProcurementCategory getCategory() {
        return category;
    }

    public ProcurementRequestStatus getStatus() {
        return status;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void markPolicyBlocked() {
        this.status = ProcurementRequestStatus.BLOCKED;
    }

    public void markPendingApproval() {
        this.status = ProcurementRequestStatus.PENDING_APPROVAL;
    }

    public void markApprovedForSourcing() {
        this.status =
                ProcurementRequestStatus.APPROVED_FOR_SOURCING;
    }

    public void markSourcing() {
        this.status = ProcurementRequestStatus.SOURCING;
    }

    public void markCompleted() {
        this.status = ProcurementRequestStatus.COMPLETED;
    }

    public void markRejected() {
        this.status = ProcurementRequestStatus.REJECTED;
    }

    public void applyExtraction(
            ProcurementExtraction extraction
    ) {

        this.itemDescription =
                extraction.itemDescription();

        this.quantity =
                extraction.quantity();

        this.budgetAmount =
                extraction.budgetAmount();

        this.currency =
                extraction.currency();

        this.deadline =
                extraction.deadline();

        this.category =
                extraction.category();
    }

    public void markReadyForPolicy() {

        this.status =
                ProcurementRequestStatus.READY_FOR_POLICY;

        this.validationMessage = null;
    }

    public void markNeedsClarification(
            String message
    ) {

        this.status =
                ProcurementRequestStatus.NEEDS_CLARIFICATION;

        this.validationMessage = message;
    }

    public void markAiFailed(
            String message
    ) {

        this.status =
                ProcurementRequestStatus.AI_FAILED;

        this.validationMessage = message;
    }
}