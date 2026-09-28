package com.procureflow.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(
            AuditLogRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public void log(
            Long requestId,
            AuditActorType actorType,
            String actorEmail,
            String action,
            String details
    ) {

        repository.save(
                new AuditLog(
                        requestId,
                        actorType,
                        actorEmail,
                        action,
                        details
                )
        );
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getForRequest(
            Long requestId
    ) {

        return repository
                .findByRequestIdOrderByCreatedAtAsc(
                        requestId
                );
    }
}