package com.taskflow.auth.application.port.in;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;

public interface LoginUseCase {
    AuthResponseDTO login(AuthRequestDTO request);
}
