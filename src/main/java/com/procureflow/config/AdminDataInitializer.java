package com.procureflow.config;

import com.procureflow.user.Role;
import com.procureflow.user.User;
import com.procureflow.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminDataInitializer {

    @Bean
    public CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email}") String adminEmail,
            @Value("${app.bootstrap-admin.password}") String adminPassword
    ) {

        return args -> {

            if (!userRepository.existsByEmailIgnoreCase(
                    adminEmail
            )) {

                User admin = new User(
                        "System Administrator",
                        adminEmail.trim().toLowerCase(),
                        passwordEncoder.encode(adminPassword),
                        Role.ADMIN
                );

                userRepository.save(admin);

                System.out.println(
                        "Bootstrap admin created: "
                                + adminEmail
                );
            }
        };
    }
}