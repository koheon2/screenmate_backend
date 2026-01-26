package moleep.screenmate.dto.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.friend.CharacterFriendMessage;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "친구 메시지 응답")
public class FriendMessageResponse {

    private UUID id;
    private UUID senderCharacterId;
    private String messageText;
    private String emoteId;
    private Instant createdAt;

    public static FriendMessageResponse from(CharacterFriendMessage message) {
        return FriendMessageResponse.builder()
                .id(message.getId())
                .senderCharacterId(message.getSenderCharacter().getId())
                .messageText(message.getMessageText())
                .emoteId(message.getEmoteId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
