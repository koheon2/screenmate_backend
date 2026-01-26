package moleep.screenmate.dto.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "친구 요청 생성")
public class FriendRequestCreateRequest {

    @Schema(description = "대상 캐릭터 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "targetCharacterId is required")
    private UUID targetCharacterId;

    @Schema(description = "요청 메시지 (한 줄)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "message is required")
    @Size(max = 200, message = "message must not exceed 200 characters")
    private String message;
}
