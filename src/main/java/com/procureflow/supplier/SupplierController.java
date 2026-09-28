package com.procureflow.supplier;

import com.procureflow.supplier.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN')")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(
            SupplierService supplierService
    ) {
        this.supplierService =
                supplierService;
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> create(
            @Valid
            @RequestBody
            CreateSupplierRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        supplierService.create(request)
                );
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAll() {

        return ResponseEntity.ok(
                supplierService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                supplierService.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateSupplierRequest request
    ) {

        return ResponseEntity.ok(
                supplierService.update(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) {

        supplierService.deactivate(id);

        return ResponseEntity.noContent()
                .build();
    }
}