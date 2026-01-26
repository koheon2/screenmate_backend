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
@Schema(description = "Google 로그인 요청")
public class GoogleLoginRequest {

    @Schema(description = "Google ID Token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Google ID token is required")
    private String idToken;

    @Schema(description = "디바이스 고유 ID", example = "device-uuid-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @Schema(description = "디바이스 이름", example = "MacBook Pro")
    private String deviceName;
}
