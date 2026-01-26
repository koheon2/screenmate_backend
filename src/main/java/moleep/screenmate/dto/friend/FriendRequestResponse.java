package moleep.screenmate.dto.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.friend.CharacterFriendRequest;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "친구 요청 응답")
public class FriendRequestResponse {

    private UUID id;
    private UUID requesterCharacterId;
    private UUID receiverCharacterId;
    private String message;
    private String status;
    private Instant createdAt;
    private Instant respondedAt;

    public static FriendRequestResponse from(CharacterFriendRequest request) {
        return FriendRequestResponse.builder()
                .id(request.getId())
                .requesterCharacterId(request.getRequesterCharacter().getId())
                .receiverCharacterId(request.getReceiverCharacter().getId())
                .message(request.getMessage())
                .status(request.getStatus().name())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .build();
    }
}
