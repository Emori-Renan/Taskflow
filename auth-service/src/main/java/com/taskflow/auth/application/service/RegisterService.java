package com.taskflow.auth.application.service;

import org.springframework.stereotype.Service;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.RegisterUseCase;
import com.taskflow.auth.application.port.out.TokenProviderPort;
import com.taskflow.auth.application.port.out.UserRepositoryPort;
import com.taskflow.auth.domain.model.User;

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
    public AuthResponseDTO register(AuthRequestDTO request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        var newUser = new User(
                request.username(),
                request.password(),
                "USER");

        var savedUser = userRepository.save(newUser);
        var token = tokenProvider.generateToken(savedUser);
        return new AuthResponseDTO(token, savedUser.username(), savedUser.role());
    }
}
