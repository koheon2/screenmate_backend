package moleep.screenmate.dto.sync;

import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.event.CharacterEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class EventResponse {

    private UUID id;
    private UUID characterId;
    private String eventType;
    private String eventText;
    private String metadata;
    private Instant createdAt;

    public static EventResponse from(CharacterEvent event) {
        return EventResponse.builder()
                .id(event.getId())
                .characterId(event.getCharacter().getId())
                .eventType(event.getEventType().name())
                .eventText(event.getEventText())
                .metadata(event.getMetadata())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
