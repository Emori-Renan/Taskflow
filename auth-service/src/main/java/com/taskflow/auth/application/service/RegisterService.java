package com.taskflow.auth.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.RegisterUseCase;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidInputException;
import com.taskflow.auth.domain.exception.UserAlreadyExistsException;
import com.taskflow.auth.domain.model.User;

import reactor.core.publisher.Mono;

@Service
public class RegisterService implements RegisterUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepositoryPort userRepository,
            TokenProviderPort tokenProvider,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<AuthResponseDTO> register(AuthRequestDTO request) {

        // 1️⃣ Validate input (reactive-safe)     
        if (request.email() == null || request.email().isBlank()) {
            return Mono.error(new InvalidInputException("Email cannot be empty."));
        }

        if (request.password() == null || request.password().isBlank()) {
            return Mono.error(new InvalidInputException("Password cannot be empty."));
        }

        // 2️⃣ Check if user already exists
        return userRepository.findByEmail(request.email())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException(request.email()));
                    }
                    return userRepository.save(
                            new User(
                                    request.email(),
                                    passwordEncoder.encode(request.password()),
                                    "USER"));
                })
                .flatMap(savedUser -> Mono.just(tokenProvider.generateToken(savedUser))
                        .map(token -> new AuthResponseDTO(token, savedUser.email(), savedUser.role())));

    }
}
