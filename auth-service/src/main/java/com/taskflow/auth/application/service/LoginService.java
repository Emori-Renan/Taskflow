package com.taskflow.auth.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.LoginUseCase;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.exception.InvalidCredentialsException;
import com.taskflow.auth.domain.exception.UserNotFoundException;

import reactor.core.publisher.Mono;

@Service
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserRepositoryPort userRepository,
                        TokenProviderPort tokenProvider,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<AuthResponseDTO> login(AuthRequestDTO request) {

        return userRepository.findByUsername(request.username())
            // 1️⃣ If user not found
            .switchIfEmpty(
                Mono.error(new UserNotFoundException("User not found"))
            )
            // 2️⃣ Validate password
            .flatMap(user -> {
                if (!passwordEncoder.matches(request.password(), user.password())) {
                    return Mono.error(new InvalidCredentialsException());
                }
                return Mono.just(user);
            })
            // 3️⃣ Generate token + response
            .map(user -> {
                String token = tokenProvider.generateToken(user);
                return new AuthResponseDTO(
                    token,
                    user.username(),
                    user.role()
                );
            });
    }
}
