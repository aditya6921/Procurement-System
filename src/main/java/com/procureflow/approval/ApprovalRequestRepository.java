package com.procureflow.approval;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRequestRepository
        extends JpaRepository<ApprovalRequest, Long> {

    long countByRequestId(Long requestId);

    long countByRequestIdAndStatus(
            Long requestId,
            ApprovalStatus status
    );

    List<ApprovalRequest>
    findByRequestIdOrderByIdAsc(
            Long requestId
    );

    List<ApprovalRequest>
    findByApprovalRoleAndStatusOrderByCreatedAtAsc(
            com.procureflow.policy.ApprovalRole role,
            ApprovalStatus status
    );

    List<ApprovalRequest> findByStatusOrderByCreatedAtAsc(ApprovalStatus status);
}
