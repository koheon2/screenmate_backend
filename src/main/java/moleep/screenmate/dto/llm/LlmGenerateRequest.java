package moleep.screenmate.dto.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LLM 생성 요청")
public class LlmGenerateRequest {

    @Schema(description = "캐릭터 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "characterId is required")
    private UUID characterId;

    @Schema(description = "사용자 메시지", example = "안녕! 오늘 기분이 어때?")
    private String userMessage;

    @Schema(description = "추가 컨텍스트 데이터")
    private Map<String, Object> context;
}
