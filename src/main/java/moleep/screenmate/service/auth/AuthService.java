package moleep.screenmate.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.config.GoogleOAuthProperties;
import moleep.screenmate.config.JwtProperties;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.domain.user.UserRepository;
import moleep.screenmate.dto.auth.AuthResponse;
import moleep.screenmate.dto.auth.GoogleLoginRequest;
import moleep.screenmate.dto.auth.RefreshTokenRequest;
import moleep.screenmate.exception.BadRequestException;
import moleep.screenmate.exception.UnauthorizedException;
import moleep.screenmate.security.jwt.JwtTokenProvider;
import moleep.screenmate.security.oauth.GoogleIdTokenVerifier;
import moleep.screenmate.security.oauth.GoogleUserInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        String idToken = request.getIdToken();
        if (idToken == null || idToken.isBlank()) {
            idToken = exchangeCodeForIdToken(request);
        }
        GoogleUserInfo googleUser = googleIdTokenVerifier.verify(idToken);

        if (!Boolean.TRUE.equals(googleUser.getEmailVerified())) {
            throw new UnauthorizedException("EMAIL_NOT_VERIFIED", "Email is not verified");
        }

        User user = userRepository.findByGoogleId(googleUser.getGoogleId())
                .map(existingUser -> {
                    existingUser.updateProfile(googleUser.getDisplayName(), googleUser.getProfileImageUrl());
                    existingUser.updateLastLogin();
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .googleId(googleUser.getGoogleId())
                            .email(googleUser.getEmail())
                            .displayName(googleUser.getDisplayName())
                            .profileImageUrl(googleUser.getProfileImageUrl())
                            .build();
                    newUser.updateLastLogin();
                    return userRepository.save(newUser);
                });

        log.info("User logged in: {} ({})", user.getEmail(), user.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenService.createOrUpdateRefreshToken(user, request.getDeviceId(), request.getDeviceName());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    private String exchangeCodeForIdToken(GoogleLoginRequest request) {
        if (request.getAuthCode() == null || request.getAuthCode().isBlank()) {
            throw new BadRequestException("GOOGLE_AUTH_CODE_REQUIRED", "Google auth code is required");
        }
        if (request.getCodeVerifier() == null || request.getCodeVerifier().isBlank()) {
            throw new BadRequestException("GOOGLE_CODE_VERIFIER_REQUIRED", "PKCE code verifier is required");
        }
        if (request.getRedirectUri() == null || request.getRedirectUri().isBlank()) {
            throw new BadRequestException("GOOGLE_REDIRECT_URI_REQUIRED", "Redirect URI is required");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", request.getAuthCode());
        form.add("client_id", googleOAuthProperties.getClientId());
        if (googleOAuthProperties.getClientSecret() != null && !googleOAuthProperties.getClientSecret().isBlank()) {
            form.add("client_secret", googleOAuthProperties.getClientSecret());
        }
        form.add("code_verifier", request.getCodeVerifier());
        form.add("redirect_uri", request.getRedirectUri());
        form.add("grant_type", "authorization_code");

        GoogleTokenResponse response = WebClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .build()
                .post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        if (response == null || response.idToken == null || response.idToken.isBlank()) {
            throw new UnauthorizedException("GOOGLE_TOKEN_EXCHANGE_FAILED", "Failed to exchange Google auth code");
        }
        return response.idToken;
    }

    private static class GoogleTokenResponse {
        @JsonProperty("id_token")
        public String idToken;
        @JsonProperty("access_token")
        public String accessToken;
        @JsonProperty("refresh_token")
        public String refreshToken;
        public String error;
        @JsonProperty("error_description")
        public String errorDescription;
    }

    @Transactional
    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
        var storedToken = tokenService.validateRefreshToken(request.getRefreshToken(), request.getDeviceId());
        User user = storedToken.getUser();

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = tokenService.createOrUpdateRefreshToken(user, request.getDeviceId(), storedToken.getDeviceName());

        log.info("Token refreshed for user: {}", user.getId());

        return buildAuthResponse(user, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(User user, String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenService.revokeRefreshTokenByRawToken(refreshToken);
        } else {
            tokenService.revokeRefreshToken(user);
        }
        log.info("User logged out: {}", user.getId());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpiryMillis() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .build();
    }
}
