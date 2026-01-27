package moleep.screenmate.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "유저 장소 발견 기록 저장")
public class PlaceDiscoverRequest {

    @Schema(description = "발견한 캐릭터 ID")
    private UUID discoveredByCharacterId;

    @Schema(description = "추가 메타데이터")
    private Map<String, Object> metadata;
}
