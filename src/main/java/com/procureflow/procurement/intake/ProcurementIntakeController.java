package com.procureflow.procurement.intake;

import com.procureflow.procurement.intake.dto.CreateProcurementRequest;
import com.procureflow.procurement.intake.dto.ProcurementIntakeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procurement/intake")
public class ProcurementIntakeController {

    private final com.procureflow.procurement.intake.ProcurementIntakeService intakeService;

    public ProcurementIntakeController(
            com.procureflow.procurement.intake.ProcurementIntakeService intakeService
    ) {
        this.intakeService = intakeService;
    }

    @PostMapping
    public ResponseEntity<ProcurementIntakeResponse> create(
            @Valid @RequestBody CreateProcurementRequest request,
            Authentication authentication
    ) {

        ProcurementIntakeResponse response =
                intakeService.create(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<java.util.List<ProcurementIntakeResponse>> getAll() {
        return ResponseEntity.ok(intakeService.getAll());
    }

    @GetMapping("/mine")
    public ResponseEntity<java.util.List<ProcurementIntakeResponse>> getMine(Authentication authentication) {
        return ResponseEntity.ok(intakeService.getMine(authentication.getName()));
    }
}
