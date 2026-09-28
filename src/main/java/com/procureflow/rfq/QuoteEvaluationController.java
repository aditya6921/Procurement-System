package com.procureflow.rfq;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rfq-evaluation")
public class QuoteEvaluationController {

    private final QuoteEvaluationService evaluationService;

    public QuoteEvaluationController(
            QuoteEvaluationService evaluationService
    ) {
        this.evaluationService =
                evaluationService;
    }

    @PostMapping("/{rfqId}")
    public ResponseEntity<QuoteEvaluationResponse>
    evaluate(
            @PathVariable Long rfqId,
            org.springframework.security.core.Authentication authentication
    ) {

        return ResponseEntity.ok(
                evaluationService.evaluate(
                        rfqId,
                        authentication.getName()
                )
        );
    }
}