package moleep.screenmate.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Schema(description = "유저 업적 진행도/해금 저장")
public class AchievementUpsertRequest {

    @Schema(description = "진행도")
    @Min(value = 0, message = "progress must be non-negative")
    private Integer progress;

    @Schema(description = "해금 시각 (즉시 해금 시 사용)")
    private Instant unlockedAt;

    @Schema(description = "추가 메타데이터")
    private Map<String, Object> metadata;
}
