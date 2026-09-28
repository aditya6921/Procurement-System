package com.procureflow.approval;

import com.procureflow.policy.ApprovalRole;
import com.procureflow.procurement.intake.ProcurementRequest;
import com.procureflow.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "approval_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_approval_request_role",
                        columnNames = {
                                "request_id",
                                "approval_role"
                        }
                )
        }
)
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "request_id",
            nullable = false
    )
    private ProcurementRequest request;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "approval_role",
            nullable = false,
            length = 30
    )
    private ApprovalRole approvalRole;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private ApprovalStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant actionedAt;

    protected ApprovalRequest() {
    }

    public ApprovalRequest(
            ProcurementRequest request,
            ApprovalRole approvalRole
    ) {
        this.request = request;
        this.approvalRole = approvalRole;
        this.status = ApprovalStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public ProcurementRequest getRequest() {
        return request;
    }

    public ApprovalRole getApprovalRole() {
        return approvalRole;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public User getApprover() {
        return approver;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getActionedAt() {
        return actionedAt;
    }

    public void approve(
            User approver,
            String comment
    ) {

        this.status = ApprovalStatus.APPROVED;
        this.approver = approver;
        this.comment = comment;
        this.actionedAt = Instant.now();
    }

    public void reject(
            User approver,
            String comment
    ) {

        this.status = ApprovalStatus.REJECTED;
        this.approver = approver;
        this.comment = comment;
        this.actionedAt = Instant.now();
    }
}