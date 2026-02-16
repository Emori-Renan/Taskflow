package com.taskflow.auth.infrastructure.adapter.in.web;

import com.taskflow.auth.application.dto.AuthResponseDTO;
import com.taskflow.auth.application.port.in.GoogleOAuthUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OAuth", description = "OAuth 2.0 login APIs")
@RestController
@RequestMapping("/api/auth/oauth")
public record OAuthController(GoogleOAuthUseCase googleOAuthUseCase) {

    @Operation(summary = "Google OAuth login", description = "Redirects to Google OAuth consent screen")
    @GetMapping("/google")
    public ResponseEntity<Void> googleLogin() {
        String authorizationUrl = googleOAuthUseCase.getAuthorizationUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authorizationUrl)
                .build();
    }

    @Operation(summary = "Google OAuth callback", description = "Handles Google OAuth callback and returns JWT tokens")
    @GetMapping("/google/callback")
    public AuthResponseDTO googleCallback(@RequestParam("code") String code) {
        return googleOAuthUseCase.handleCallback(code);
    }
}
