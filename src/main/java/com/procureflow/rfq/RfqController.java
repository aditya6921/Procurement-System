package com.procureflow.rfq;

import com.procureflow.rfq.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfqs")
public class RfqController {

    private final RfqService rfqService;

    public RfqController(
            RfqService rfqService
    ) {
        this.rfqService = rfqService;
    }

    @PostMapping("/request/{requestId}")
    public ResponseEntity<RfqResponse> create(
            @PathVariable Long requestId,

            @Valid
            @RequestBody
            CreateRfqRequest request,

            Authentication authentication
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        rfqService.create(
                                requestId,
                                request,
                                authentication.getName()
                        )
                );
    }

    @PostMapping("/{rfqId}/open")
    public ResponseEntity<RfqResponse> open(
            @PathVariable Long rfqId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                rfqService.open(
                        rfqId,
                        authentication.getName()
                )
        );
    }

    @PostMapping("/{rfqId}/quotes")
    public ResponseEntity<QuoteResponse> submitQuote(
            @PathVariable Long rfqId,

            @Valid
            @RequestBody
            SubmitQuoteRequest request,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                rfqService.submitQuote(
                        rfqId,
                        request,
                        authentication.getName()
                )
        );
    }

    @PostMapping("/{rfqId}/close")
    public ResponseEntity<RfqResponse> close(
            @PathVariable Long rfqId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                rfqService.close(
                        rfqId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{rfqId}")
    public ResponseEntity<RfqResponse> get(
            @PathVariable Long rfqId
    ) {

        return ResponseEntity.ok(
                rfqService.get(rfqId)
        );
    }

    @GetMapping("/{rfqId}/quotes")
    public ResponseEntity<List<QuoteResponse>>
    getQuotes(
            @PathVariable Long rfqId
    ) {

        return ResponseEntity.ok(
                rfqService.getQuotes(rfqId)
        );
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<RfqResponse>>
    getForRequest(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                rfqService.getForRequest(
                        requestId
                )
        );
    }
}