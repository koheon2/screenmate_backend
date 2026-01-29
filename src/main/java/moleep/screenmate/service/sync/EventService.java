package moleep.screenmate.service.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.event.CharacterEvent;
import moleep.screenmate.domain.event.CharacterEventRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.sync.EventCreateRequest;
import moleep.screenmate.dto.sync.EventResponse;
import moleep.screenmate.exception.BadRequestException;
import moleep.screenmate.validation.CharacterValidator;
import moleep.screenmate.validation.OwnershipValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final CharacterEventRepository eventRepository;
    private final OwnershipValidator ownershipValidator;
    private final CharacterValidator characterValidator;
    private final ObjectMapper objectMapper;

    @Transactional
    public EventResponse createEvent(UUID characterId, User user, EventCreateRequest request) {
        Character character = ownershipValidator.validateAndGetCharacter(characterId, user);

        characterValidator.validateEventText(request.getEventText());

        String metadataJson = null;
        if (request.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(request.getMetadata());
            } catch (JsonProcessingException e) {
                throw new BadRequestException("INVALID_METADATA", "metadata must be valid JSON");
            }
        }

        CharacterEvent event = CharacterEvent.builder()
                .character(character)
                .eventType(request.getEventType())
                .eventText(request.getEventText())
                .metadata(metadataJson)
                .build();

        event = eventRepository.save(event);
        log.info("Created event: {} for character: {}", event.getId(), characterId);

        return EventResponse.from(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEvents(UUID characterId, User user, int limit) {
        ownershipValidator.validateOwnership(characterId, user);

        return eventRepository.findByCharacterIdOrderByCreatedAtDesc(characterId, PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
    }
}
