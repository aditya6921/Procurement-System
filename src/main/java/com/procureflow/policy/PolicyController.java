package com.procureflow.policy;

import com.procureflow.supplier.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN')")
public class PolicyController {

    private final PolicyEngineService policyEngineService;

    public PolicyController(
            PolicyEngineService policyEngineService
    ) {
        this.policyEngineService =
                policyEngineService;
    }

    @PostMapping(
            "/evaluate/{requestId}"
    )
    public ResponseEntity<
            PolicyEvaluationResponse
            > evaluate(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                policyEngineService.evaluate(
                        requestId
                )
        );
    }

    @GetMapping(
            "/evaluation/{requestId}"
    )
    public ResponseEntity<
            PolicyEvaluationResponse
            > latest(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                policyEngineService
                        .getLatestEvaluation(
                                requestId
                        )
        );
    }

    @GetMapping(
            "/eligible-suppliers/{requestId}"
    )
    public ResponseEntity<
            List<Supplier>
            > eligibleSuppliers(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                policyEngineService
                        .getEligibleSuppliers(
                                requestId
                        )
        );
    }
}