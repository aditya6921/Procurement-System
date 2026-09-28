package com.procureflow.rfq;

import com.procureflow.audit.AuditActorType;
import com.procureflow.audit.AuditService;
import com.procureflow.exception.ConflictException;
import com.procureflow.exception.ResourceNotFoundException;
import com.procureflow.policy.PolicyDecision;
import com.procureflow.policy.PolicyEvaluation;
import com.procureflow.policy.PolicyEvaluationRepository;
import com.procureflow.procurement.intake.ProcurementRequest;
import com.procureflow.procurement.intake.ProcurementRequestRepository;
import com.procureflow.procurement.intake.ProcurementRequestStatus;
import com.procureflow.supplier.Supplier;
import com.procureflow.supplier.SupplierRepository;
import com.procureflow.user.User;
import com.procureflow.user.UserRepository;
import com.procureflow.rfq.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RfqService {

    private final RfqRepository rfqRepository;
    private final RfqSupplierRepository rfqSupplierRepository;
    private final SupplierQuoteRepository quoteRepository;
    private final ProcurementRequestRepository requestRepository;
    private final PolicyEvaluationRepository policyEvaluationRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public RfqService(
            RfqRepository rfqRepository,
            RfqSupplierRepository rfqSupplierRepository,
            SupplierQuoteRepository quoteRepository,
            ProcurementRequestRepository requestRepository,
            PolicyEvaluationRepository policyEvaluationRepository,
            SupplierRepository supplierRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {

        this.rfqRepository = rfqRepository;
        this.rfqSupplierRepository = rfqSupplierRepository;
        this.quoteRepository = quoteRepository;
        this.requestRepository = requestRepository;
        this.policyEvaluationRepository =
                policyEvaluationRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public RfqResponse create(
            Long requestId,
            CreateRfqRequest input,
            String email
    ) {

        User creator = findUser(email);

        ProcurementRequest request =
                requestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Request not found"
                                )
                        );

        if (request.getStatus()
                != ProcurementRequestStatus.APPROVED_FOR_SOURCING) {

            throw new IllegalStateException(
                    "Request is not approved for sourcing"
            );
        }

        PolicyEvaluation evaluation =
                policyEvaluationRepository
                        .findFirstByRequestIdOrderByEvaluatedAtDesc(
                                requestId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Request has not been evaluated by policy engine"
                                )
                        );

        if (evaluation.getDecision()
                != PolicyDecision.APPROVED_FOR_SOURCING) {

            throw new IllegalStateException(
                    "Request has not passed policy"
            );
        }

        List<Supplier> eligibleSuppliers =
                findEligibleSuppliersForEvaluation(
                        request,
                        evaluation
                );

        if (eligibleSuppliers.isEmpty()) {

            throw new IllegalStateException(
                    "No eligible suppliers are available"
            );
        }

        String title =
                input.title() == null
                        || input.title().isBlank()
                        ? "RFQ - "
                        + request.getItemDescription()
                        : input.title().trim();

        Rfq rfq =
                new Rfq(
                        request,
                        creator,
                        title,
                        input.description(),
                        input.quotationDeadline()
                );

        rfq = rfqRepository.save(rfq);

        for (Supplier supplier : eligibleSuppliers) {

            rfqSupplierRepository.save(
                    new RfqSupplier(
                            rfq,
                            supplier
                    )
            );
        }

        request.markSourcing();

        requestRepository.save(request);

        auditService.log(
                requestId,
                AuditActorType.USER,
                email,
                "RFQ_CREATED",
                "RFQ="
                        + rfq.getId()
                        + ", invitedSuppliers="
                        + eligibleSuppliers.size()
        );

        return RfqResponse.from(rfq);
    }

    @Transactional
    public RfqResponse open(
            Long rfqId,
            String email
    ) {

        Rfq rfq = findRfq(rfqId);

        if (rfq.getStatus()
                != RfqStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only draft RFQs can be opened"
            );
        }

        if (!rfq.getQuotationDeadline()
                .isAfter(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "Quotation deadline has already passed"
            );
        }

        rfq.open();

        rfqRepository.save(rfq);

        auditService.log(
                rfq.getRequest().getId(),
                AuditActorType.USER,
                email,
                "RFQ_OPENED",
                "RFQ=" + rfqId
        );

        return RfqResponse.from(rfq);
    }

    @Transactional
    public QuoteResponse submitQuote(
            Long rfqId,
            SubmitQuoteRequest input,
            String email
    ) {

        Rfq rfq = findRfq(rfqId);

        if (rfq.getStatus()
                != RfqStatus.OPEN) {

            throw new IllegalStateException(
                    "RFQ is not open"
            );
        }

        if (LocalDateTime.now()
                .isAfter(
                        rfq.getQuotationDeadline()
                )) {

            throw new IllegalStateException(
                    "Quotation deadline has passed"
            );
        }

        ProcurementRequest request =
                rfq.getRequest();

        if (!input.currency()
                .equalsIgnoreCase(
                        request.getCurrency()
                )) {

            throw new IllegalArgumentException(
                    "Quote currency must match request currency"
            );
        }

        Supplier supplier =
                supplierRepository
                        .findById(input.supplierId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Supplier not found"
                                )
                        );

        RfqSupplier invitation =
                rfqSupplierRepository
                        .findByRfqIdAndSupplierId(
                                rfqId,
                                supplier.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Supplier was not invited to this RFQ"
                                )
                        );

        if (quoteRepository
                .existsByRfqIdAndSupplierId(
                        rfqId,
                        supplier.getId()
                )) {

            throw new ConflictException(
                    "Supplier has already submitted a quote"
            );
        }

        SupplierQuote quote =
                new SupplierQuote(
                        rfq,
                        supplier,
                        input.quotedAmount(),
                        input.currency().toUpperCase(),
                        input.deliveryDays(),
                        input.warrantyMonths(),
                        input.paymentTerms(),
                        input.notes()
                );

        quote = quoteRepository.save(quote);

        invitation.markQuoted();

        rfqSupplierRepository.save(invitation);

        auditService.log(
                request.getId(),
                AuditActorType.USER,
                email,
                "QUOTE_SUBMITTED",
                "RFQ="
                        + rfqId
                        + ", supplier="
                        + supplier.getCompanyName()
        );

        return QuoteResponse.from(quote);
    }

    @Transactional
    public RfqResponse close(
            Long rfqId,
            String email
    ) {

        Rfq rfq = findRfq(rfqId);

        if (rfq.getStatus()
                != RfqStatus.OPEN) {

            throw new IllegalStateException(
                    "Only open RFQs can be closed"
            );
        }

        rfq.close();

        rfqRepository.save(rfq);

        auditService.log(
                rfq.getRequest().getId(),
                AuditActorType.USER,
                email,
                "RFQ_CLOSED",
                "RFQ=" + rfqId
        );

        return RfqResponse.from(rfq);
    }

    @Transactional(readOnly = true)
    public RfqResponse get(
            Long rfqId
    ) {

        return RfqResponse.from(
                findRfq(rfqId)
        );
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> getQuotes(
            Long rfqId
    ) {

        findRfq(rfqId);

        return quoteRepository
                .findByRfqId(rfqId)
                .stream()
                .map(QuoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RfqResponse> getForRequest(
            Long requestId
    ) {

        return rfqRepository
                .findByRequestIdOrderByCreatedAtDesc(
                        requestId
                )
                .stream()
                .map(RfqResponse::from)
                .toList();
    }

    private List<Supplier>
    findEligibleSuppliersForEvaluation(
            ProcurementRequest request,
            PolicyEvaluation evaluation
    ) {

        /*
         * We already know how many suppliers passed policy.
         * For the actual list, use the policy's constraints.
         */
        var policy =
                evaluation.getPolicy();

        return supplierRepository
                .findAllByCategoriesContainingAndActiveTrue(
                        request.getCategory()
                )
                .stream()
                .filter(supplier ->
                        supplier.getSupplierStatus()
                                == com.procureflow.supplier.SupplierStatus.ACTIVE
                )
                .filter(supplier ->
                        supplier.getRiskScore()
                                <= policy.getMaximumSupplierRiskScore()
                )
                .filter(supplier ->
                        supplier.getRating()
                                .compareTo(
                                        policy.getMinimumSupplierRating()
                                ) >= 0
                )
                .filter(supplier ->
                        !policy.isRequireApprovedSupplier()
                                || supplier.isApprovedSupplier()
                )
                .filter(supplier ->
                        !policy.isRequireCompliantSupplier()
                                || supplier.getComplianceStatus()
                                == com.procureflow.supplier.ComplianceStatus.COMPLIANT
                )
                .toList();
    }

    private Rfq findRfq(Long id) {

        return rfqRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "RFQ not found: " + id
                        )
                );
    }

    private User findUser(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }
}