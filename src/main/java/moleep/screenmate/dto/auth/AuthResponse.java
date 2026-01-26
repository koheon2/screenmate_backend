package moleep.screenmate.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "인증 응답")
public class AuthResponse {

    @Schema(description = "Access Token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh Token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "Access Token 만료 시간 (초)", example = "900")
    private long expiresIn;

    @Schema(description = "사용자 정보")
    private UserInfo user;

    @Getter
    @Builder
    @Schema(description = "사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID id;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "표시 이름", example = "홍길동")
        private String displayName;

        @Schema(description = "프로필 이미지 URL", example = "https://lh3.googleusercontent.com/...")
        private String profileImageUrl;
    }
}
