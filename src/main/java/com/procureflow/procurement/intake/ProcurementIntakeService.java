package com.procureflow.procurement.intake;

import com.procureflow.exception.AiExtractionException;
import com.procureflow.procurement.intake.ai.ProcurementExtractionService;
import com.procureflow.procurement.intake.dto.CreateProcurementRequest;
import com.procureflow.procurement.intake.dto.ProcurementExtraction;
import com.procureflow.procurement.intake.dto.ProcurementIntakeResponse;
import com.procureflow.user.User;
import com.procureflow.user.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ProcurementIntakeService {

    private final ProcurementRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ProcurementExtractionService extractionService;
    private final Validator validator;

    public ProcurementIntakeService(
            ProcurementRequestRepository requestRepository,
            UserRepository userRepository,
            ProcurementExtractionService extractionService,
            Validator validator
    ) {

        this.requestRepository =
                requestRepository;

        this.userRepository =
                userRepository;

        this.extractionService =
                extractionService;

        this.validator =
                validator;
    }

    public ProcurementIntakeResponse create(
            CreateProcurementRequest request,
            String authenticatedEmail
    ) {

        User requester =
                userRepository
                        .findByEmailIgnoreCase(
                                authenticatedEmail
                        )
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Authenticated user not found"
                                )
                        );

        ProcurementRequest procurementRequest =
                new ProcurementRequest(
                        requester,
                        request.request().trim()
                );

        procurementRequest =
                requestRepository.save(
                        procurementRequest
                );

        ProcurementExtraction extraction;

        try {

            extraction =
                    extractionService.extract(
                            procurementRequest.getRawRequest()
                    );

        } catch (AiExtractionException ex) {

            procurementRequest.markAiFailed(
                    "AI extraction failed. Please retry the request."
            );

            requestRepository.save(
                    procurementRequest
            );

            throw ex;
        }

        Set<ConstraintViolation<ProcurementExtraction>>
                violations =
                validator.validate(extraction);

        if (!violations.isEmpty()) {

            List<String> errors =
                    violations
                            .stream()
                            .map(ConstraintViolation::getMessage)
                            .distinct()
                            .toList();

            String message =
                    String.join(
                            "; ",
                            errors
                    );

            procurementRequest.applyExtraction(
                    extraction
            );

            procurementRequest.markNeedsClarification(
                    message
            );

            requestRepository.save(
                    procurementRequest
            );

            return ProcurementIntakeResponse.from(
                    procurementRequest,
                    extraction,
                    "Additional information is required before this request can continue.",
                    errors
            );
        }

        procurementRequest.applyExtraction(
                extraction
        );

        procurementRequest.markReadyForPolicy();

        requestRepository.save(
                procurementRequest
        );

        return ProcurementIntakeResponse.from(
                procurementRequest,
                extraction,
                "Procurement request successfully extracted and validated.",
                List.of()
        );
    }

    public ProcurementRequest getById(
            Long id
    ) {

        return requestRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Procurement request not found: " + id
                        )
                );
    }

    public List<ProcurementIntakeResponse> getAll() {
        return requestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(request -> ProcurementIntakeResponse.from(
                        request,
                        new ProcurementExtraction(request.getItemDescription(), request.getQuantity(),
                                request.getBudgetAmount(), request.getCurrency(), request.getDeadline(), request.getCategory()),
                        request.getValidationMessage(),
                        List.of()))
                .toList();
    }

    public List<ProcurementIntakeResponse> getMine(String email) {
        User requester = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Authenticated user not found"));
        return requestRepository.findByRequesterOrderByCreatedAtDesc(requester).stream()
                .map(request -> ProcurementIntakeResponse.from(request,
                        new ProcurementExtraction(request.getItemDescription(), request.getQuantity(), request.getBudgetAmount(), request.getCurrency(), request.getDeadline(), request.getCategory()),
                        request.getValidationMessage(), List.of()))
                .toList();
    }
}
