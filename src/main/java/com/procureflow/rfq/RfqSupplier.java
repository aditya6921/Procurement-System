package com.procureflow.rfq;

import com.procureflow.supplier.Supplier;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "rfq_suppliers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rfq_supplier",
                        columnNames = {
                                "rfq_id",
                                "supplier_id"
                        }
                )
        }
)
public class RfqSupplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RfqSupplierStatus status;

    @Column(nullable = false, updatable = false)
    private Instant invitedAt;

    private Instant respondedAt;

    protected RfqSupplier() {
    }

    public RfqSupplier(
            Rfq rfq,
            Supplier supplier
    ) {
        this.rfq = rfq;
        this.supplier = supplier;
        this.status = RfqSupplierStatus.INVITED;
        this.invitedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Rfq getRfq() {
        return rfq;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public RfqSupplierStatus getStatus() {
        return status;
    }

    public void markQuoted() {
        status = RfqSupplierStatus.QUOTED;
        respondedAt = Instant.now();
    }

    public void reject() {
        status = RfqSupplierStatus.REJECTED;
    }
}