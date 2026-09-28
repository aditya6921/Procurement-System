package com.procureflow.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record PolicyEvaluationResponse(

        Long evaluationId,

        Long requestId,

        String policyName,

        BigDecimal budgetAmount,

        PolicyDecision decision,

        String reason,

        Integer eligibleSupplierCount,

        Set<ApprovalRole> approvalRoles,

        Instant evaluatedAt

) {

    public static PolicyEvaluationResponse from(
            PolicyEvaluation evaluation
    ) {

        return new PolicyEvaluationResponse(
                evaluation.getId(),
                evaluation.getRequest().getId(),
                evaluation.getPolicy().getName(),
                evaluation.getRequest()
                        .getBudgetAmount(),
                evaluation.getDecision(),
                evaluation.getReason(),
                evaluation.getEligibleSupplierCount(),
                evaluation.getApprovalRoles(),
                evaluation.getEvaluatedAt()
        );
    }
}