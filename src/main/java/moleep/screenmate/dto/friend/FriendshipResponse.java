package moleep.screenmate.dto.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.friend.CharacterFriendship;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "친구 관계 응답")
public class FriendshipResponse {

    private UUID id;
    private UUID characterId;
    private UUID friendCharacterId;
    private String friendName;
    private String friendSpecies;
    private String friendPersonality;
    private String friendOwnerDisplayName;
    private Integer intimacy;
    private Instant createdAt;
    private Instant updatedAt;

    public static FriendshipResponse from(CharacterFriendship friendship, Character self, Character friend) {
        return FriendshipResponse.builder()
                .id(friendship.getId())
                .characterId(self.getId())
                .friendCharacterId(friend.getId())
                .friendName(friend.getName())
                .friendSpecies(friend.getSpecies())
                .friendPersonality(friend.getPersonality())
                .friendOwnerDisplayName(friend.getUser().getDisplayName())
                .intimacy(friendship.getIntimacy())
                .createdAt(friendship.getCreatedAt())
                .updatedAt(friendship.getUpdatedAt())
                .build();
    }
}
