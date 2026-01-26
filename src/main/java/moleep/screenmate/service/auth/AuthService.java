package moleep.screenmate.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.config.JwtProperties;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.domain.user.UserRepository;
import moleep.screenmate.dto.auth.AuthResponse;
import moleep.screenmate.dto.auth.GoogleLoginRequest;
import moleep.screenmate.dto.auth.RefreshTokenRequest;
import moleep.screenmate.exception.UnauthorizedException;
import moleep.screenmate.security.jwt.JwtTokenProvider;
import moleep.screenmate.security.oauth.GoogleIdTokenVerifier;
import moleep.screenmate.security.oauth.GoogleUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfo googleUser = googleIdTokenVerifier.verify(request.getIdToken());

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
