package com.taskflow.auth.application.service;

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

    public RegisterService(UserRepositoryPort userRepository,
                           TokenProviderPort tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<AuthResponseDTO> register(AuthRequestDTO request) {

        // 1️⃣ Validate input (reactive-safe)
        if (request.username() == null || request.username().isBlank()) {
            return Mono.error(new InvalidInputException("Username cannot be empty."));
        }

        if (request.password() == null || request.password().isBlank()) {
            return Mono.error(new InvalidInputException("Password cannot be empty."));
        }

        // 2️⃣ Check if user already exists
        return userRepository.findByUsername(request.username())
            .hasElement()
            .flatMap(exists ->{
                if (exists) {
                    return Mono.error(new UserAlreadyExistsException(request.username()));
                } 
            return userRepository.save(
                    new User(
                        request.username(),
                        request.password(),
                        "USER"
                    )
                );
            })
            .map(savedUser -> {
                String token = tokenProvider.generateToken(savedUser);
                return new AuthResponseDTO(
                    token,
                    savedUser.username(),
                    savedUser.role()
                );
            });
            
    }
}
