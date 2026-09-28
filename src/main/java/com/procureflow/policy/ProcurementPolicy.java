package com.procureflow.policy;

import com.procureflow.procurement.intake.ProcurementCategory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "procurement_policies")
public class ProcurementPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ProcurementCategory category;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal maximumBudget;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal managerApprovalThreshold;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal procurementApprovalThreshold;

    @Column(
            nullable = false,
            precision = 3,
            scale = 2
    )
    private BigDecimal minimumSupplierRating;

    @Column(
            nullable = false
    )
    private Integer maximumSupplierRiskScore;

    @Column(
            nullable = false
    )
    private boolean requireApprovedSupplier;

    @Column(
            nullable = false
    )
    private boolean requireCompliantSupplier;

    @Column(
            nullable = false
    )
    private boolean active;

    @Column(
            nullable = false
    )
    private Integer priority;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected ProcurementPolicy() {
    }

    public ProcurementPolicy(
            String name,
            ProcurementCategory category,
            BigDecimal maximumBudget,
            BigDecimal managerApprovalThreshold,
            BigDecimal procurementApprovalThreshold,
            BigDecimal minimumSupplierRating,
            Integer maximumSupplierRiskScore,
            boolean requireApprovedSupplier,
            boolean requireCompliantSupplier,
            boolean active,
            Integer priority
    ) {

        this.name = name;
        this.category = category;
        this.maximumBudget = maximumBudget;
        this.managerApprovalThreshold =
                managerApprovalThreshold;
        this.procurementApprovalThreshold =
                procurementApprovalThreshold;
        this.minimumSupplierRating =
                minimumSupplierRating;
        this.maximumSupplierRiskScore =
                maximumSupplierRiskScore;
        this.requireApprovedSupplier =
                requireApprovedSupplier;
        this.requireCompliantSupplier =
                requireCompliantSupplier;
        this.active = active;
        this.priority = priority;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt =
                Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProcurementCategory getCategory() {
        return category;
    }

    public BigDecimal getMaximumBudget() {
        return maximumBudget;
    }

    public BigDecimal getManagerApprovalThreshold() {
        return managerApprovalThreshold;
    }

    public BigDecimal getProcurementApprovalThreshold() {
        return procurementApprovalThreshold;
    }

    public BigDecimal getMinimumSupplierRating() {
        return minimumSupplierRating;
    }

    public Integer getMaximumSupplierRiskScore() {
        return maximumSupplierRiskScore;
    }

    public boolean isRequireApprovedSupplier() {
        return requireApprovedSupplier;
    }

    public boolean isRequireCompliantSupplier() {
        return requireCompliantSupplier;
    }

    public boolean isActive() {
        return active;
    }

    public Integer getPriority() {
        return priority;
    }
}