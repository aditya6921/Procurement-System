package com.procureflow.rfq;

import com.procureflow.procurement.intake.ProcurementRequest;
import com.procureflow.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "rfqs")
public class Rfq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private ProcurementRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime quotationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RfqStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Rfq() {
    }

    public Rfq(
            ProcurementRequest request,
            User createdBy,
            String title,
            String description,
            LocalDateTime quotationDeadline
    ) {
        this.request = request;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.quotationDeadline = quotationDeadline;
        this.status = RfqStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public ProcurementRequest getRequest() {
        return request;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getQuotationDeadline() {
        return quotationDeadline;
    }

    public RfqStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void open() {
        status = RfqStatus.OPEN;
    }

    public void close() {
        status = RfqStatus.CLOSED;
    }

    public void award() {
        status = RfqStatus.AWARDED;
    }

    public void cancel() {
        status = RfqStatus.CANCELLED;
    }
}