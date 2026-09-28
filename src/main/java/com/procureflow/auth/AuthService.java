package com.procureflow.auth;

import com.procureflow.auth.dto.AuthResponse;
import com.procureflow.auth.dto.LoginRequest;
import com.procureflow.auth.dto.RegisterRequest;
import com.procureflow.auth.dto.UserResponse;
import com.procureflow.exception.ConflictException;
import com.procureflow.security.JwtService;
import com.procureflow.user.Role;
import com.procureflow.user.User;
import com.procureflow.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long expirationMinutes;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${security.jwt.expiration-minutes}")
            long expirationMinutes
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional
    public AuthResponse register(
            RegisterRequest request
    ) {

        String normalizedEmail =
                normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(
                normalizedEmail
        )) {
            throw new ConflictException(
                    "An account with this email already exists"
            );
        }

        User user = new User(
                request.fullName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                Role.EMPLOYEE
        );

        User savedUser =
                userRepository.save(user);

        String token =
                jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                "Bearer",
                expirationMinutes * 60,
                UserResponse.from(savedUser)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(
            LoginRequest request
    ) {

        String normalizedEmail =
                normalizeEmail(request.email());

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.password()
                    )
            );

        } catch (AuthenticationException ex) {

            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        User user =
                userRepository
                        .findByEmailIgnoreCase(
                                normalizedEmail
                        )
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        "Invalid email or password"
                                )
                        );

        String token =
                jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                expirationMinutes * 60,
                UserResponse.from(user)
        );
    }

    private String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase();
    }
}