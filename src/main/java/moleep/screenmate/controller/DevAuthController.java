package moleep.screenmate.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moleep.screenmate.config.JwtProperties;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.domain.user.UserRepository;
import moleep.screenmate.dto.auth.AuthResponse;
import moleep.screenmate.security.jwt.JwtTokenProvider;
import moleep.screenmate.service.auth.TokenService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 개발/테스트 환경 전용 인증 컨트롤러
 * 프로덕션에서는 절대 사용하지 마세요!
 */
@Tag(name = "Dev Auth", description = "개발용 인증 API (프로덕션 비활성화)")
@RestController
@RequestMapping("/dev/auth")
@RequiredArgsConstructor
@Profile("!prod")  // prod 프로파일에서는 비활성화
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @Operation(summary = "테스트 유저 생성 및 토큰 발급",
               description = "개발 환경에서 테스트용 유저를 생성하고 토큰을 발급합니다. 프로덕션에서는 비활성화됩니다.")
    @PostMapping("/test-login")
    public ResponseEntity<AuthResponse> testLogin(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "test-device-001") String deviceId) {

        // 기존 유저 찾거나 새로 생성
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .googleId("test-google-id-" + UUID.randomUUID())
                            .email(email)
                            .displayName("Test User")
                            .build();
                    newUser.updateLastLogin();
                    return userRepository.save(newUser);
                });

        user.updateLastLogin();
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenService.createOrUpdateRefreshToken(user, deviceId, "Test Device");

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpiryMillis() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .build());
    }
}
