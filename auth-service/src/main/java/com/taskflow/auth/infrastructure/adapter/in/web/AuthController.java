package com.taskflow.auth.infrastructure.adapter.in.web;

import com.taskflow.auth.application.dto.AuthRequestDTO;
import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.AuthUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public record AuthController(AuthUseCase authUseCase) {

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody AuthRequestDTO request) {
        return authUseCase.login(request);
    }
}
