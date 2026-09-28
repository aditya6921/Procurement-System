package com.procureflow.approval;

import com.procureflow.policy.ApprovalRole;

import java.time.Instant;

public record ApprovalResponse(

        Long id,

        Long requestId,

        ApprovalRole approvalRole,

        ApprovalStatus status,

        String approverEmail,

        String comment,

        Instant createdAt,

        Instant actionedAt

) {

    public static ApprovalResponse from(
            ApprovalRequest approval
    ) {

        return new ApprovalResponse(
                approval.getId(),
                approval.getRequest().getId(),
                approval.getApprovalRole(),
                approval.getStatus(),
                approval.getApprover() == null
                        ? null
                        : approval.getApprover().getEmail(),
                approval.getComment(),
                approval.getCreatedAt(),
                approval.getActionedAt()
        );
    }
}