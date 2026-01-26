package moleep.screenmate.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.token.RefreshToken;
import moleep.screenmate.domain.token.RefreshTokenRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.exception.InvalidTokenException;
import moleep.screenmate.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String createOrUpdateRefreshToken(User user, String deviceId, String deviceName) {
        String rawToken = jwtTokenProvider.generateRefreshToken(user.getId(), deviceId);
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = jwtTokenProvider.getRefreshTokenExpiry();

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());

        if (existingToken.isPresent()) {
            RefreshToken token = existingToken.get();
            token.updateToken(tokenHash, deviceId, deviceName, expiresAt);
            refreshTokenRepository.save(token);
            log.info("Updated refresh token for user: {}", user.getId());
        } else {
            RefreshToken token = RefreshToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .deviceId(deviceId)
                    .deviceName(deviceName)
                    .expiresAt(expiresAt)
                    .build();
            refreshTokenRepository.save(token);
            log.info("Created new refresh token for user: {}", user.getId());
        }

        return rawToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken, String deviceId) {
        if (!jwtTokenProvider.isRefreshToken(rawToken)) {
            throw new InvalidTokenException("Token is not a refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(rawToken);
        String tokenHash = hashToken(rawToken);

        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!storedToken.getTokenHash().equals(tokenHash)) {
            throw new InvalidTokenException("Refresh token does not match");
        }

        if (!storedToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        if (!storedToken.getDeviceId().equals(deviceId)) {
            throw new InvalidTokenException("Device ID does not match. Session may have been invalidated by login on another device.");
        }

        return storedToken;
    }

    @Transactional
    public void revokeRefreshToken(User user) {
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                    log.info("Revoked refresh token for user: {}", user.getId());
                });
    }

    @Transactional
    public void revokeRefreshTokenByRawToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                    log.info("Revoked refresh token by token hash");
                });
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
