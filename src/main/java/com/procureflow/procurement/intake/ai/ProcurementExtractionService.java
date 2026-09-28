package com.procureflow.procurement.intake.ai;

import com.procureflow.procurement.intake.dto.ProcurementExtraction;

public interface ProcurementExtractionService {

    ProcurementExtraction extract(
            String rawRequest
    );
}