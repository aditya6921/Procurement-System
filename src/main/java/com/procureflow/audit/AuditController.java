package com.procureflow.audit;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(
            AuditService auditService
    ) {
        this.auditService = auditService;
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<AuditLog>> getForRequest(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                auditService.getForRequest(requestId)
        );
    }
}