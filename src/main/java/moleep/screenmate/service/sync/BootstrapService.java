package moleep.screenmate.service.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.character.CharacterRepository;
import moleep.screenmate.domain.event.CharacterEvent;
import moleep.screenmate.domain.event.CharacterEventRepository;
import moleep.screenmate.domain.memory.CharacterQaMemory;
import moleep.screenmate.domain.memory.CharacterQaMemoryRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.sync.BootstrapResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BootstrapService {

    private static final int RECENT_EVENTS_LIMIT = 10;

    private final CharacterRepository characterRepository;
    private final CharacterQaMemoryRepository qaMemoryRepository;
    private final CharacterEventRepository eventRepository;

    @Transactional(readOnly = true)
    public BootstrapResponse getBootstrapData(User user) {
        log.info("Fetching bootstrap data for user: {}", user.getId());

        List<Character> characters = characterRepository.findByUserId(user.getId());

        Map<UUID, CharacterQaMemory> qaMemories = characters.stream()
                .map(c -> qaMemoryRepository.findByCharacterId(c.getId()).orElse(null))
                .filter(m -> m != null)
                .collect(Collectors.toMap(m -> m.getCharacter().getId(), m -> m));

        Map<UUID, List<CharacterEvent>> recentEvents = characters.stream()
                .collect(Collectors.toMap(
                        Character::getId,
                        c -> eventRepository.findByCharacterIdOrderByCreatedAtDesc(
                                c.getId(), PageRequest.of(0, RECENT_EVENTS_LIMIT)).getContent()
                ));

        List<BootstrapResponse.CharacterData> characterDataList = characters.stream()
                .map(c -> mapCharacterData(c, qaMemories.get(c.getId()), recentEvents.getOrDefault(c.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return BootstrapResponse.builder()
                .user(BootstrapResponse.UserData.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .profileImageUrl(user.getProfileImageUrl())
                        .createdAt(user.getCreatedAt())
                        .lastLoginAt(user.getLastLoginAt())
                        .build())
                .characters(characterDataList)
                .build();
    }

    private BootstrapResponse.CharacterData mapCharacterData(Character character, CharacterQaMemory qaMemory, List<CharacterEvent> events) {
        BootstrapResponse.QaMemoryData qaMemoryData = null;
        if (qaMemory != null) {
            qaMemoryData = BootstrapResponse.QaMemoryData.builder()
                    .data(qaMemory.getQaData())
                    .version(qaMemory.getVersion())
                    .build();
        }

        List<BootstrapResponse.EventData> eventDataList = events.stream()
                .map(e -> BootstrapResponse.EventData.builder()
                        .id(e.getId())
                        .eventType(e.getEventType().name())
                        .eventText(e.getEventText())
                        .createdAt(e.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return BootstrapResponse.CharacterData.builder()
                .id(character.getId())
                .name(character.getName())
                .species(character.getSpecies())
                .inviteCode(character.getInviteCode())
                .personality(character.getPersonality())
                .stageIndex(character.getStageIndex())
                .happiness(character.getHappiness())
                .hunger(character.getHunger())
                .health(character.getHealth())
                .aggressionGauge(character.getAggressionGauge())
                .isAlive(character.getIsAlive())
                .diedAt(character.getDiedAt())
                .totalPlayTimeSeconds(character.getTotalPlayTimeSeconds())
                .lastFedAt(character.getLastFedAt())
                .lastPlayedAt(character.getLastPlayedAt())
                .createdAt(character.getCreatedAt())
                .updatedAt(character.getUpdatedAt())
                .version(character.getVersion())
                .qaMemory(qaMemoryData)
                .recentEvents(eventDataList)
                .build();
    }
}
