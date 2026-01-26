package moleep.screenmate.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 갱신 요청")
public class RefreshTokenRequest {

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    @Schema(description = "디바이스 고유 ID (로그인 시 사용한 것과 동일해야 함)", example = "device-uuid-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Device ID is required")
    private String deviceId;
}
