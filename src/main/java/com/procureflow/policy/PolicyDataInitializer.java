package com.procureflow.policy;

import com.procureflow.procurement.intake.ProcurementCategory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class PolicyDataInitializer {

    @Bean
    public CommandLineRunner initializePolicies(
            ProcurementPolicyRepository repository
    ) {

        return args -> {

            if (repository.count() > 0) {
                return;
            }

            /*
             * IT Hardware Policy
             */
            repository.save(
                    new ProcurementPolicy(
                            "IT Hardware Standard Procurement Policy",

                            ProcurementCategory.IT_HARDWARE,

                            new BigDecimal("10000000"),

                            new BigDecimal("100000"),

                            new BigDecimal("1000000"),

                            new BigDecimal("3.50"),

                            60,

                            true,

                            true,

                            true,

                            1
                    )
            );

            /*
             * Software Policy
             */
            repository.save(
                    new ProcurementPolicy(
                            "Software Procurement Policy",

                            ProcurementCategory.SOFTWARE,

                            new BigDecimal("5000000"),

                            new BigDecimal("100000"),

                            new BigDecimal("500000"),

                            new BigDecimal("3.50"),

                            50,

                            true,

                            true,

                            true,

                            2
                    )
            );

            /*
             * Global fallback policy
             */
            repository.save(
                    new ProcurementPolicy(
                            "Global Procurement Policy",

                            null,

                            new BigDecimal("2500000"),

                            new BigDecimal("100000"),

                            new BigDecimal("500000"),

                            new BigDecimal("3.00"),

                            70,

                            true,

                            true,

                            true,

                            100
                    )
            );
        };
    }
}