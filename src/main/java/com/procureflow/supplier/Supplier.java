package com.procureflow.supplier;

import com.procureflow.procurement.intake.ProcurementCategory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "suppliers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_suppliers_email",
                        columnNames = "contact_email"
                )
        }
)
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 200
    )
    private String companyName;

    @Column(
            name = "contact_email",
            nullable = false,
            length = 150
    )
    private String contactEmail;

    @Column(length = 100)
    private String contactPerson;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "supplier_categories",
            joinColumns = @JoinColumn(
                    name = "supplier_id"
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 50
    )
    private Set<ProcurementCategory> categories =
            new HashSet<>();

    @Column(
            precision = 3,
            scale = 2,
            nullable = false
    )
    private BigDecimal rating;

    @Column(
            nullable = false
    )
    private Integer riskScore;

    @Column(
            nullable = false
    )
    private Integer averageDeliveryDays;

    @Column(
            precision = 5,
            scale = 2,
            nullable = false
    )
    private BigDecimal deliveryPerformance;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private ComplianceStatus complianceStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private SupplierStatus supplierStatus;

    @Column(
            nullable = false
    )
    private boolean approvedSupplier;

    @Column(
            nullable = false
    )
    private boolean active;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            nullable = false
    )
    private Instant updatedAt;

    protected Supplier() {
    }

    public Supplier(
            String companyName,
            String contactEmail,
            String contactPerson,
            Set<ProcurementCategory> categories,
            BigDecimal rating,
            Integer riskScore,
            Integer averageDeliveryDays,
            BigDecimal deliveryPerformance,
            ComplianceStatus complianceStatus,
            SupplierStatus supplierStatus,
            boolean approvedSupplier
    ) {

        this.companyName =
                companyName;

        this.contactEmail =
                contactEmail;

        this.contactPerson =
                contactPerson;

        this.categories =
                new HashSet<>(categories);

        this.rating =
                rating;

        this.riskScore =
                riskScore;

        this.averageDeliveryDays =
                averageDeliveryDays;

        this.deliveryPerformance =
                deliveryPerformance;

        this.complianceStatus =
                complianceStatus;

        this.supplierStatus =
                supplierStatus;

        this.approvedSupplier =
                approvedSupplier;

        this.active =
                true;
    }

    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt =
                Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public Set<ProcurementCategory> getCategories() {
        return categories;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public Integer getAverageDeliveryDays() {
        return averageDeliveryDays;
    }

    public BigDecimal getDeliveryPerformance() {
        return deliveryPerformance;
    }

    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }

    public SupplierStatus getSupplierStatus() {
        return supplierStatus;
    }

    public boolean isApprovedSupplier() {
        return approvedSupplier;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSupplierStatus(
            SupplierStatus supplierStatus
    ) {
        this.supplierStatus = supplierStatus;
    }

    public void update(
            String companyName,
            String contactEmail,
            String contactPerson,
            Set<ProcurementCategory> categories,
            BigDecimal rating,
            Integer riskScore,
            Integer averageDeliveryDays,
            BigDecimal deliveryPerformance,
            ComplianceStatus complianceStatus,
            SupplierStatus supplierStatus,
            boolean approvedSupplier,
            boolean active
    ) {

        this.companyName =
                companyName;

        this.contactEmail =
                contactEmail;

        this.contactPerson =
                contactPerson;

        this.categories =
                new HashSet<>(categories);

        this.rating =
                rating;

        this.riskScore =
                riskScore;

        this.averageDeliveryDays =
                averageDeliveryDays;

        this.deliveryPerformance =
                deliveryPerformance;

        this.complianceStatus =
                complianceStatus;

        this.supplierStatus =
                supplierStatus;

        this.approvedSupplier =
                approvedSupplier;

        this.active =
                active;
    }
}