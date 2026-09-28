package com.procureflow.audit;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(
                        name = "idx_audit_request",
                        columnList = "request_id"
                ),
                @Index(
                        name = "idx_audit_created",
                        columnList = "created_at"
                )
        }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditActorType actorType;

    @Column(nullable = false, length = 150)
    private String actorEmail;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(length = 4000)
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected AuditLog() {
    }

    public AuditLog(
            Long requestId,
            AuditActorType actorType,
            String actorEmail,
            String action,
            String details
    ) {
        this.requestId = requestId;
        this.actorType = actorType;
        this.actorEmail = actorEmail;
        this.action = action;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public AuditActorType getActorType() {
        return actorType;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}