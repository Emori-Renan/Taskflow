package com.taskflow.auth.application.port.in;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;

import reactor.core.publisher.Mono;

public interface LoginUseCase {
    Mono<AuthResponseDTO> login(AuthRequestDTO request);
}
