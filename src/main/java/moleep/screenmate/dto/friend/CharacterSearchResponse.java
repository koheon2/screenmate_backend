package moleep.screenmate.dto.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.character.Character;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "캐릭터 검색 결과")
public class CharacterSearchResponse {

    private UUID characterId;
    private String name;
    private String species;
    private String inviteCode;
    private String personality;
    private Boolean isAlive;
    private String ownerDisplayName;

    public static CharacterSearchResponse from(Character character) {
        return CharacterSearchResponse.builder()
                .characterId(character.getId())
                .name(character.getName())
                .species(character.getSpecies())
                .inviteCode(character.getInviteCode())
                .personality(character.getPersonality())
                .isAlive(character.getIsAlive())
                .ownerDisplayName(character.getUser().getDisplayName())
                .build();
    }
}
