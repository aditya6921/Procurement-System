package com.procureflow.approval;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(
            ApprovalService approvalService
    ) {
        this.approvalService = approvalService;
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<ApprovalResponse>>
    getForRequest(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                approvalService.getForRequest(
                        requestId
                )
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalResponse>>
    getPending(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                approvalService.getPendingForUser(
                        authentication.getName()
                )
        );
    }

    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<ApprovalResponse> approve(
            @PathVariable Long approvalId,

            @Valid
            @RequestBody
            ApprovalActionRequest request,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                approvalService.approve(
                        approvalId,
                        authentication.getName(),
                        request.comment()
                )
        );
    }

    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<ApprovalResponse> reject(
            @PathVariable Long approvalId,

            @Valid
            @RequestBody
            ApprovalActionRequest request,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                approvalService.reject(
                        approvalId,
                        authentication.getName(),
                        request.comment()
                )
        );
    }
}