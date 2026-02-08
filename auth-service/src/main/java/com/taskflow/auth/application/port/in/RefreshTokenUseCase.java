package com.taskflow.auth.application.port.in;

import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.dto.RefreshRequestDTO;

public interface RefreshTokenUseCase {
    AuthResponseDTO refresh(RefreshRequestDTO request);
}
