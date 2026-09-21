package com.soa.auth.service;

import com.soa.auth.dto.GoogleTokenResponse;
import com.soa.auth.dto.GoogleUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class GoogleOAuthService {

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    @Value("${app.google.redirect-uri}")
    private String defaultRedirectUri;

    private final RestClient restClient = RestClient.builder().build();

    public String buildAuthorizationUrl(String redirectUri, String state) {
        String effectiveRedirectUri = (redirectUri != null && !redirectUri.isBlank())
                ? redirectUri
                : defaultRedirectUri;

        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", effectiveRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state != null ? state : "login")
                .build()
                .toUriString();
    }

    public GoogleTokenResponse exchangeCode(String code, String redirectUri) {
        String effectiveRedirectUri = (redirectUri != null && !redirectUri.isBlank())
                ? redirectUri
                : defaultRedirectUri;

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", effectiveRedirectUri);
        body.add("grant_type", "authorization_code");

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    public GoogleUserInfo getUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfo.class);
    }

    public void revokeToken(String token) {
        if (token == null || token.isBlank())
            return;
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("token", token);

            restClient.post()
                    .uri("https://oauth2.googleapis.com/revoke")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Đã gửi yêu cầu thu hồi token thành công tới Google OAuth Server.");
        } catch (Exception e) {
            log.warn("Không thể thu hồi token phía Google (token có thể đã hết hạn): {}", e.getMessage());
        }
    }

    public void updateCredentials(String clientId, String clientSecret, String redirectUri) {
        if (clientId != null && !clientId.isBlank()) {
            this.clientId = clientId.trim();
        }
        if (clientSecret != null && !clientSecret.isBlank()) {
            this.clientSecret = clientSecret.trim();
        }
        if (redirectUri != null && !redirectUri.isBlank()) {
            this.defaultRedirectUri = redirectUri.trim();
        }
        log.info("Đã cập nhật Google OAuth credentials tại runtime: Client ID = {}", this.clientId);
    }

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank() && !clientId.contains("YOUR_GOOGLE_CLIENT_ID");
    }

    public String getClientId() {
        return clientId;
    }

    public String getDefaultRedirectUri() {
        return defaultRedirectUri;
    }

    public boolean hasClientSecret() {
        return clientSecret != null && !clientSecret.isBlank() && !clientSecret.contains("YOUR_GOOGLE_CLIENT_SECRET");
    }
}
