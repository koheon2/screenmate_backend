package moleep.screenmate.dto.lineage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.lineage.CharacterLineage;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "가계도 부모-자식 엣지")
public class LineageEdgeResponse {

    private UUID id;
    private UUID childCharacterId;
    private UUID parentACharacterId;
    private UUID parentBCharacterId;
    private Instant createdAt;

    public static LineageEdgeResponse from(CharacterLineage lineage) {
        return LineageEdgeResponse.builder()
                .id(lineage.getId())
                .childCharacterId(lineage.getChildCharacter().getId())
                .parentACharacterId(lineage.getParentACharacter().getId())
                .parentBCharacterId(lineage.getParentBCharacter().getId())
                .createdAt(lineage.getCreatedAt())
                .build();
    }
}

