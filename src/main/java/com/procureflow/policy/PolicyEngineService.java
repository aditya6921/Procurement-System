package com.procureflow.policy;

import com.procureflow.audit.AuditActorType;
import com.procureflow.audit.AuditService;
import com.procureflow.approval.ApprovalService;
import com.procureflow.exception.ResourceNotFoundException;
import com.procureflow.procurement.intake.*;
import com.procureflow.supplier.Supplier;
import com.procureflow.supplier.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PolicyEngineService {

    private final ProcurementRequestRepository requestRepository;
    private final ProcurementPolicyRepository policyRepository;
    private final PolicyEvaluationRepository evaluationRepository;
    private final SupplierService supplierService;
    private final ApprovalService approvalService;
    private final AuditService auditService;

    public PolicyEngineService(
            ProcurementRequestRepository requestRepository,
            ProcurementPolicyRepository policyRepository,
            PolicyEvaluationRepository evaluationRepository,
            SupplierService supplierService,
            ApprovalService approvalService,
            AuditService auditService
    ) {

        this.requestRepository = requestRepository;
        this.policyRepository = policyRepository;
        this.evaluationRepository = evaluationRepository;
        this.supplierService = supplierService;
        this.approvalService = approvalService;
        this.auditService = auditService;
    }

    @Transactional
    public PolicyEvaluationResponse evaluate(
            Long requestId
    ) {

        ProcurementRequest request =
                requestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Procurement request not found: "
                                                + requestId
                                )
                        );

        if (request.getStatus()
                != ProcurementRequestStatus.READY_FOR_POLICY) {

            throw new IllegalStateException(
                    "Only READY_FOR_POLICY requests can be evaluated"
            );
        }

        ProcurementPolicy policy =
                policyRepository
                        .findFirstByCategoryAndActiveTrueOrderByPriorityAsc(
                                request.getCategory()
                        )
                        .orElseGet(() ->
                                policyRepository
                                        .findFirstByCategoryIsNullAndActiveTrueOrderByPriorityAsc()
                                        .orElseThrow(() ->
                                                new IllegalStateException(
                                                        "No active procurement policy configured"
                                                )
                                        )
                        );

        Set<ApprovalRole> approvalRoles =
                determineApprovalRoles(
                        request.getBudgetAmount(),
                        policy
                );

        List<Supplier> eligibleSuppliers =
                supplierService.findEligibleSuppliers(
                        request.getCategory(),
                        policy.getMaximumSupplierRiskScore(),
                        policy.getMinimumSupplierRating(),
                        policy.isRequireApprovedSupplier(),
                        policy.isRequireCompliantSupplier()
                );

        BigDecimal budget =
                request.getBudgetAmount();

        PolicyEvaluation evaluation;

        if (budget.compareTo(
                policy.getMaximumBudget()
        ) > 0) {

            request.markPolicyBlocked();

            evaluation =
                    new PolicyEvaluation(
                            request,
                            policy,
                            PolicyDecision.BLOCKED,
                            "Budget exceeds policy maximum of ₹"
                                    + policy.getMaximumBudget(),
                            eligibleSuppliers.size(),
                            approvalRoles
                    );

        } else if (eligibleSuppliers.isEmpty()) {

            request.markPolicyBlocked();

            evaluation =
                    new PolicyEvaluation(
                            request,
                            policy,
                            PolicyDecision.BLOCKED,
                            "No compliant eligible supplier was found.",
                            0,
                            approvalRoles
                    );

        } else {

            PolicyDecision decision =
                    approvalRoles.isEmpty()
                            ? PolicyDecision.APPROVED_FOR_SOURCING
                            : PolicyDecision.NEEDS_APPROVAL;

            evaluation =
                    new PolicyEvaluation(
                            request,
                            policy,
                            decision,
                            buildReason(
                                    approvalRoles,
                                    eligibleSuppliers.size()
                            ),
                            eligibleSuppliers.size(),
                            approvalRoles
                    );

            if (approvalRoles.isEmpty()) {

                request.markApprovedForSourcing();

            } else {

                approvalService.initializeApprovals(
                        request,
                        approvalRoles
                );
            }
        }

        requestRepository.save(request);

        PolicyEvaluation saved =
                evaluationRepository.save(
                        evaluation
                );

        auditService.log(
                requestId,
                AuditActorType.SYSTEM,
                "SYSTEM",
                "POLICY_EVALUATED",
                "Decision="
                        + saved.getDecision()
                        + ", eligibleSuppliers="
                        + saved.getEligibleSupplierCount()
        );

        return PolicyEvaluationResponse.from(
                saved
        );
    }

    @Transactional(readOnly = true)
    public PolicyEvaluationResponse getLatestEvaluation(
            Long requestId
    ) {

        return evaluationRepository
                .findFirstByRequestIdOrderByEvaluatedAtDesc(
                        requestId
                )
                .map(PolicyEvaluationResponse::from)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No policy evaluation found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Supplier> getEligibleSuppliers(
            Long requestId
    ) {

        ProcurementRequest request =
                requestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Request not found"
                                )
                        );

        ProcurementPolicy policy =
                policyRepository
                        .findFirstByCategoryAndActiveTrueOrderByPriorityAsc(
                                request.getCategory()
                        )
                        .orElseGet(() ->
                                policyRepository
                                        .findFirstByCategoryIsNullAndActiveTrueOrderByPriorityAsc()
                                        .orElseThrow(() ->
                                                new IllegalStateException(
                                                        "No policy configured"
                                                )
                                        )
                        );

        return supplierService.findEligibleSuppliers(
                request.getCategory(),
                policy.getMaximumSupplierRiskScore(),
                policy.getMinimumSupplierRating(),
                policy.isRequireApprovedSupplier(),
                policy.isRequireCompliantSupplier()
        );
    }

    private Set<ApprovalRole> determineApprovalRoles(
            BigDecimal budget,
            ProcurementPolicy policy
    ) {

        Set<ApprovalRole> roles =
                new LinkedHashSet<>();

        if (budget.compareTo(
                policy.getManagerApprovalThreshold()
        ) > 0) {

            roles.add(
                    ApprovalRole.MANAGER
            );
        }

        if (budget.compareTo(
                policy.getProcurementApprovalThreshold()
        ) > 0) {

            roles.add(
                    ApprovalRole.PROCUREMENT
            );

            roles.add(
                    ApprovalRole.FINANCE
            );
        }

        return roles;
    }

    private String buildReason(
            Set<ApprovalRole> roles,
            int supplierCount
    ) {

        if (roles.isEmpty()) {

            return "Policy passed. "
                    + supplierCount
                    + " eligible supplier(s) found.";
        }

        return "Policy passed but approval is required. "
                + "Required roles="
                + roles
                + ", eligible suppliers="
                + supplierCount;
    }
}