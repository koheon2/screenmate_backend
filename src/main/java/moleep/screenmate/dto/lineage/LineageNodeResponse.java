package moleep.screenmate.dto.lineage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.character.Character;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "가계도 노드")
public class LineageNodeResponse {

    private UUID id;
    private String name;
    private String species;
    private String inviteCode;
    private String ownerDisplayName;
    private Instant createdAt;

    public static LineageNodeResponse from(Character character) {
        return LineageNodeResponse.builder()
                .id(character.getId())
                .name(character.getName())
                .species(character.getSpecies())
                .inviteCode(character.getInviteCode())
                .ownerDisplayName(character.getUser().getDisplayName())
                .createdAt(character.getCreatedAt())
                .build();
    }
}

