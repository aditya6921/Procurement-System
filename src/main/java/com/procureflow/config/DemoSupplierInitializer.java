package com.procureflow.config;

import com.procureflow.procurement.intake.ProcurementCategory;
import com.procureflow.supplier.ComplianceStatus;
import com.procureflow.supplier.Supplier;
import com.procureflow.supplier.SupplierRepository;
import com.procureflow.supplier.SupplierStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.EnumSet;

@Configuration
public class DemoSupplierInitializer {
    @Bean
    public CommandLineRunner initializeDemoSupplier(SupplierRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Supplier(
                        "ProcureFlow Demo Supply Co.", "demo-supplier@procureflow.example", "Demo Supplier",
                        EnumSet.allOf(ProcurementCategory.class), new BigDecimal("4.50"), 10, 14,
                        new BigDecimal("95.00"), ComplianceStatus.COMPLIANT, SupplierStatus.ACTIVE, true));
            }
        };
    }
}
