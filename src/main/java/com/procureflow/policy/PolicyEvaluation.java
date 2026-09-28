package com.procureflow.policy;

import com.procureflow.procurement.intake.ProcurementRequest;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "policy_evaluations",
        indexes = {
                @Index(
                        name = "idx_policy_eval_request",
                        columnList = "request_id"
                )
        }
)
public class PolicyEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "request_id",
            nullable = false
    )
    private ProcurementRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "policy_id",
            nullable = false
    )
    private ProcurementPolicy policy;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private PolicyDecision decision;

    @Column(
            nullable = false,
            length = 2000
    )
    private String reason;

    @Column(
            nullable = false
    )
    private Integer eligibleSupplierCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "policy_approval_roles",
            joinColumns = @JoinColumn(
                    name = "policy_evaluation_id"
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(
            name = "approval_role",
            nullable = false,
            length = 30
    )
    private Set<ApprovalRole> approvalRoles =
            new HashSet<>();

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant evaluatedAt;

    protected PolicyEvaluation() {
    }

    public PolicyEvaluation(
            ProcurementRequest request,
            ProcurementPolicy policy,
            PolicyDecision decision,
            String reason,
            Integer eligibleSupplierCount,
            Set<ApprovalRole> approvalRoles
    ) {

        this.request = request;
        this.policy = policy;
        this.decision = decision;
        this.reason = reason;
        this.eligibleSupplierCount =
                eligibleSupplierCount;
        this.approvalRoles =
                new HashSet<>(approvalRoles);
    }

    @PrePersist
    protected void onCreate() {
        this.evaluatedAt =
                Instant.now();
    }

    public Long getId() {
        return id;
    }

    public ProcurementRequest getRequest() {
        return request;
    }

    public ProcurementPolicy getPolicy() {
        return policy;
    }

    public PolicyDecision getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public Integer getEligibleSupplierCount() {
        return eligibleSupplierCount;
    }

    public Set<ApprovalRole> getApprovalRoles() {
        return approvalRoles;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void approveForSourcing() {
        this.decision = PolicyDecision.APPROVED_FOR_SOURCING;
        this.reason = "Policy passed and all required approvals are complete.";
    }
}
