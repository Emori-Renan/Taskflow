package com.taskflow.auth.application.port.in;

import com.taskflow.auth.application.dto.AuthResponseDTO;

public interface GoogleOAuthUseCase {
    String getAuthorizationUrl();
    AuthResponseDTO handleCallback(String code);
}
