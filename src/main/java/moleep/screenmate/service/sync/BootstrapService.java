package moleep.screenmate.service.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.achievement.UserAchievement;
import moleep.screenmate.domain.achievement.UserAchievementRepository;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.character.CharacterRepository;
import moleep.screenmate.domain.event.CharacterEvent;
import moleep.screenmate.domain.event.CharacterEventRepository;
import moleep.screenmate.domain.friend.CharacterFriendRequest;
import moleep.screenmate.domain.friend.CharacterFriendRequestRepository;
import moleep.screenmate.domain.friend.CharacterFriendship;
import moleep.screenmate.domain.friend.CharacterFriendshipRepository;
import moleep.screenmate.domain.memory.CharacterQaMemory;
import moleep.screenmate.domain.memory.CharacterQaMemoryRepository;
import moleep.screenmate.domain.place.UserDiscoveredPlace;
import moleep.screenmate.domain.place.UserDiscoveredPlaceRepository;
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
    private final UserAchievementRepository userAchievementRepository;
    private final UserDiscoveredPlaceRepository userDiscoveredPlaceRepository;
    private final CharacterFriendshipRepository friendshipRepository;
    private final CharacterFriendRequestRepository friendRequestRepository;

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

        Map<UUID, List<CharacterFriendship>> friendships = characters.stream()
                .collect(Collectors.toMap(Character::getId, c -> friendshipRepository.findByCharacterId(c.getId())));

        Map<UUID, List<CharacterFriendRequest>> incomingRequests = characters.stream()
                .collect(Collectors.toMap(Character::getId,
                        c -> friendRequestRepository.findIncomingByCharacterIdAndStatus(c.getId(), CharacterFriendRequest.Status.PENDING)));

        List<BootstrapResponse.CharacterData> characterDataList = characters.stream()
                .map(c -> mapCharacterData(
                        c,
                        qaMemories.get(c.getId()),
                        recentEvents.getOrDefault(c.getId(), Collections.emptyList()),
                        friendships.getOrDefault(c.getId(), Collections.emptyList()),
                        incomingRequests.getOrDefault(c.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());

        List<BootstrapResponse.AchievementData> achievements = userAchievementRepository.findByUserIdWithDefinition(user.getId()).stream()
                .map(this::mapAchievementData)
                .collect(Collectors.toList());

        List<BootstrapResponse.DiscoveredPlaceData> discoveredPlaces = userDiscoveredPlaceRepository.findByUserIdWithDefinition(user.getId()).stream()
                .map(this::mapDiscoveredPlaceData)
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
                .achievements(achievements)
                .discoveredPlaces(discoveredPlaces)
                .build();
    }

    private BootstrapResponse.CharacterData mapCharacterData(
            Character character,
            CharacterQaMemory qaMemory,
            List<CharacterEvent> events,
            List<CharacterFriendship> friendships,
            List<CharacterFriendRequest> incomingRequests
    ) {
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

        List<BootstrapResponse.FriendData> friendDataList = friendships.stream()
                .map(f -> mapFriendData(character, f))
                .collect(Collectors.toList());

        List<BootstrapResponse.FriendRequestData> requestDataList = incomingRequests.stream()
                .map(this::mapFriendRequestData)
                .collect(Collectors.toList());

        return BootstrapResponse.CharacterData.builder()
                .id(character.getId())
                .name(character.getName())
                .species(character.getSpecies())
                .inviteCode(character.getInviteCode())
                .homePlaceId(character.getHomePlaceId())
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
                .friends(friendDataList)
                .incomingFriendRequests(requestDataList)
                .build();
    }

    private BootstrapResponse.FriendData mapFriendData(Character self, CharacterFriendship friendship) {
        Character friend = friendship.getCharacterA().getId().equals(self.getId())
                ? friendship.getCharacterB()
                : friendship.getCharacterA();
        return BootstrapResponse.FriendData.builder()
                .friendshipId(friendship.getId())
                .friendCharacterId(friend.getId())
                .friendName(friend.getName())
                .friendSpecies(friend.getSpecies())
                .friendInviteCode(friend.getInviteCode())
                .intimacy(friendship.getIntimacy())
                .updatedAt(friendship.getUpdatedAt())
                .build();
    }

    private BootstrapResponse.FriendRequestData mapFriendRequestData(CharacterFriendRequest request) {
        Character requester = request.getRequesterCharacter();
        return BootstrapResponse.FriendRequestData.builder()
                .id(request.getId())
                .requesterCharacterId(requester.getId())
                .requesterName(requester.getName())
                .requesterSpecies(requester.getSpecies())
                .requesterInviteCode(requester.getInviteCode())
                .message(request.getMessage())
                .status(request.getStatus().name())
                .createdAt(request.getCreatedAt())
                .build();
    }

    private BootstrapResponse.AchievementData mapAchievementData(UserAchievement achievement) {
        return BootstrapResponse.AchievementData.builder()
                .id(achievement.getId())
                .achievementId(achievement.getAchievement().getId())
                .name(achievement.getAchievement().getName())
                .description(achievement.getAchievement().getDescription())
                .category(achievement.getAchievement().getCategory())
                .points(achievement.getAchievement().getPoints())
                .hidden(achievement.getAchievement().getHidden())
                .progress(achievement.getProgress())
                .unlockedAt(achievement.getUnlockedAt())
                .metadata(achievement.getMetadata())
                .build();
    }

    private BootstrapResponse.DiscoveredPlaceData mapDiscoveredPlaceData(UserDiscoveredPlace place) {
        return BootstrapResponse.DiscoveredPlaceData.builder()
                .id(place.getId())
                .placeId(place.getPlace().getId())
                .name(place.getPlace().getName())
                .region(place.getPlace().getRegion())
                .rarity(place.getPlace().getRarity())
                .discoveredAt(place.getDiscoveredAt())
                .discoveredByCharacterId(place.getDiscoveredByCharacter() != null ? place.getDiscoveredByCharacter().getId() : null)
                .metadata(place.getMetadata())
                .build();
    }
}
