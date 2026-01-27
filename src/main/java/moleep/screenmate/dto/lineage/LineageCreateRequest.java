package moleep.screenmate.dto.lineage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "가계도 관계 생성 요청")
public class LineageCreateRequest {

    @NotNull
    @Schema(description = "부모 A 캐릭터 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID parentACharacterId;

    @NotNull
    @Schema(description = "부모 B 캐릭터 ID", example = "d0f6a5a0-3b5a-4e4f-9e84-8b6d3c3b9a77")
    private UUID parentBCharacterId;
}

