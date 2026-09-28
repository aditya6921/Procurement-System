package com.procureflow.policy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyEvaluationRepository
        extends JpaRepository<
        PolicyEvaluation,
        Long> {

    Optional<PolicyEvaluation>
    findFirstByRequestIdOrderByEvaluatedAtDesc(
            Long requestId
    );
}