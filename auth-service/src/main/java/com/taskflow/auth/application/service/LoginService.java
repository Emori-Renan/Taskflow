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
    public AuthResponseDTO login(AuthRequestDTO request) {
            var user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (!passwordEncoder.matches(request.password(), user.password())) {
                throw new InvalidCredentialsException();
            }

            var token = tokenProvider.generateToken(user);
            return new AuthResponseDTO(token, user.username(), user.role());
    }
}
