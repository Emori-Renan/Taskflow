package com.taskflow.auth.infrastructure.adapter.out.oauth;

import com.taskflow.auth.application.dto.GoogleUserInfoDTO;
import com.taskflow.auth.application.port.out.GoogleOAuthPort;
import com.taskflow.auth.domain.exception.OAuthException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class GoogleOAuthAdapter implements GoogleOAuthPort {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOAuthAdapter(
            @Value("${oauth.google.client-id}") String clientId,
            @Value("${oauth.google.client-secret}") String clientSecret,
            @Value("${oauth.google.redirect-uri}") String redirectUri) {
        this.restClient = RestClient.create();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                + "&access_type=offline";
    }

    @Override
    @SuppressWarnings("unchecked")
    public GoogleUserInfoDTO exchangeCodeForUserInfo(String code) {
        Map<String, Object> tokenResponse;
        try {
            tokenResponse = restClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                            + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                            + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                            + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                            + "&grant_type=authorization_code")
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new OAuthException("Failed to exchange authorization code with Google: " + e.getMessage());
        }

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new OAuthException("Invalid token response from Google");
        }

        String accessToken = (String) tokenResponse.get("access_token");

        Map<String, Object> userInfo;
        try {
            userInfo = restClient.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new OAuthException("Failed to fetch user info from Google: " + e.getMessage());
        }

        if (userInfo == null || !userInfo.containsKey("email")) {
            throw new OAuthException("Invalid user info response from Google");
        }

        return new GoogleUserInfoDTO(
                (String) userInfo.get("email"),
                (String) userInfo.get("name"),
                (String) userInfo.get("picture")
        );
    }
}
