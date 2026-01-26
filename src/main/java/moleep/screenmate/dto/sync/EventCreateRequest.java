package moleep.screenmate.dto.sync;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moleep.screenmate.domain.event.CharacterEvent;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이벤트 생성 요청")
public class EventCreateRequest {

    @Schema(description = "이벤트 타입", example = "SPEAKING", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"EVOLUTION", "DEATH", "FEEDING", "PLAYING", "SPEAKING", "EMOTION", "MILESTONE", "CUSTOM"})
    @NotNull(message = "eventType is required")
    private CharacterEvent.EventType eventType;

    @Schema(description = "이벤트 내용", example = "사용자에게 '안녕하세요!'라고 인사했습니다", maxLength = 1000)
    @Size(max = 1000, message = "eventText must not exceed 1000 characters")
    private String eventText;

    @Schema(description = "추가 메타데이터 (JSON)", example = "{\"emotion\": \"happy\", \"target\": \"user\"}")
    private String metadata;
}
