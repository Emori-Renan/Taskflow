package com.taskflow.auth.application.port.in;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;

public interface AuthUseCase {
    AuthResponseDTO login(AuthRequestDTO request);
    AuthResponseDTO register(AuthRequestDTO request);
}