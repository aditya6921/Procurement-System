package com.procureflow.approval;

import com.procureflow.audit.AuditActorType;
import com.procureflow.audit.AuditService;
import com.procureflow.exception.ResourceNotFoundException;
import com.procureflow.policy.ApprovalRole;
import com.procureflow.policy.PolicyEvaluationRepository;
import com.procureflow.procurement.intake.ProcurementRequest;
import com.procureflow.procurement.intake.ProcurementRequestRepository;
import com.procureflow.user.Role;
import com.procureflow.user.User;
import com.procureflow.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class ApprovalService {

    private final ApprovalRequestRepository approvalRepository;
    private final ProcurementRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final PolicyEvaluationRepository evaluationRepository;

    public ApprovalService(
            ApprovalRequestRepository approvalRepository,
            ProcurementRequestRepository requestRepository,
            UserRepository userRepository,
            AuditService auditService,
            PolicyEvaluationRepository evaluationRepository
    ) {

        this.approvalRepository = approvalRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public void initializeApprovals(
            ProcurementRequest request,
            Set<ApprovalRole> roles
    ) {

        if (roles.isEmpty()) {
            request.markApprovedForSourcing();
            requestRepository.save(request);
            return;
        }

        if (approvalRepository.countByRequestId(
                request.getId()
        ) > 0) {
            return;
        }

        roles.stream()
                .sorted(
                        Comparator.comparingInt(
                                ApprovalRole::getOrder
                        )
                )
                .forEach(role ->
                        approvalRepository.save(
                                new ApprovalRequest(
                                        request,
                                        role
                                )
                        )
                );

        request.markPendingApproval();

        requestRepository.save(request);

        auditService.log(
                request.getId(),
                AuditActorType.SYSTEM,
                "SYSTEM",
                "APPROVALS_INITIALIZED",
                "Required approvals: " + roles
        );
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> getForRequest(
            Long requestId
    ) {

        return approvalRepository
                .findByRequestIdOrderByIdAsc(requestId)
                .stream()
                .map(ApprovalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> getPendingForUser(
            String email
    ) {

        User user =
                findUser(email);

        if (user.getRole() == Role.ADMIN) {
            return approvalRepository.findByStatusOrderByCreatedAtAsc(ApprovalStatus.PENDING)
                    .stream().map(ApprovalResponse::from).toList();
        }

        ApprovalRole role =
                mapRole(user.getRole());

        if (role == null) {
            return List.of();
        }

        return approvalRepository
                .findByApprovalRoleAndStatusOrderByCreatedAtAsc(
                        role,
                        ApprovalStatus.PENDING
                )
                .stream()
                .map(ApprovalResponse::from)
                .toList();
    }

    @Transactional
    public ApprovalResponse approve(
            Long approvalId,
            String email,
            String comment
    ) {

        return action(
                approvalId,
                email,
                comment,
                true
        );
    }

    @Transactional
    public ApprovalResponse reject(
            Long approvalId,
            String email,
            String comment
    ) {

        return action(
                approvalId,
                email,
                comment,
                false
        );
    }

    private ApprovalResponse action(
            Long approvalId,
            String email,
            String comment,
            boolean approve
    ) {

        ApprovalRequest approval =
                approvalRepository
                        .findById(approvalId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Approval not found: "
                                                + approvalId
                                )
                        );

        User user =
                findUser(email);

        if (approval.getStatus()
                != ApprovalStatus.PENDING) {

            throw new IllegalStateException(
                    "Approval is no longer pending"
            );
        }

        if (approval.getRequest()
                .getRequester()
                .getId()
                .equals(user.getId())) {

            throw new IllegalStateException(
                    "Requester cannot approve or reject their own request"
            );
        }

        boolean authorized =
                user.getRole() == Role.ADMIN
                        || mapRole(user.getRole())
                        == approval.getApprovalRole();

        if (!authorized) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized for this approval"
            );
        }

        if (approve) {

            ensurePreviousApprovalsComplete(
                    approval
            );

            approval.approve(
                    user,
                    comment
            );

        } else {

            approval.reject(
                    user,
                    comment
            );

            approval.getRequest()
                    .markRejected();
        }

        approvalRepository.save(
                approval
        );

        ProcurementRequest request =
                approval.getRequest();

        if (approve) {

            long pendingCount =
                    approvalRepository
                            .countByRequestIdAndStatus(
                                    request.getId(),
                                    ApprovalStatus.PENDING
                            );

            if (pendingCount == 0) {

                request.markApprovedForSourcing();
                evaluationRepository.findFirstByRequestIdOrderByEvaluatedAtDesc(request.getId())
                        .ifPresent(evaluation -> {
                            evaluation.approveForSourcing();
                            evaluationRepository.save(evaluation);
                        });

                auditService.log(
                        request.getId(),
                        AuditActorType.SYSTEM,
                        "SYSTEM",
                        "ALL_APPROVALS_COMPLETED",
                        "Request approved for sourcing"
                );
            }
        }

        requestRepository.save(request);

        auditService.log(
                request.getId(),
                AuditActorType.USER,
                user.getEmail(),
                approve
                        ? "APPROVAL_APPROVED"
                        : "APPROVAL_REJECTED",
                "Approval role="
                        + approval.getApprovalRole()
                        + ", comment="
                        + comment
        );

        return ApprovalResponse.from(
                approval
        );
    }

    private void ensurePreviousApprovalsComplete(
            ApprovalRequest current
    ) {

        List<ApprovalRequest> approvals =
                approvalRepository
                        .findByRequestIdOrderByIdAsc(
                                current.getRequest().getId()
                        );

        for (ApprovalRequest approval : approvals) {

            if (approval
                    .getApprovalRole()
                    .getOrder()
                    < current
                    .getApprovalRole()
                    .getOrder()) {

                if (approval.getStatus()
                        != ApprovalStatus.APPROVED) {

                    throw new IllegalStateException(
                            "Previous approval must be completed first"
                    );
                }
            }
        }
    }

    private ApprovalRole mapRole(
            Role role
    ) {

        return switch (role) {

            case MANAGER ->
                    ApprovalRole.MANAGER;

            case PROCUREMENT ->
                    ApprovalRole.PROCUREMENT;

            case FINANCE ->
                    ApprovalRole.FINANCE;

            default ->
                    null;
        };
    }

    private User findUser(
            String email
    ) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }
}
