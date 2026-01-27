package moleep.screenmate.service.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.achievement.AchievementDefinition;
import moleep.screenmate.domain.achievement.AchievementDefinitionRepository;
import moleep.screenmate.domain.achievement.UserAchievement;
import moleep.screenmate.domain.achievement.UserAchievementRepository;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.place.PlaceDefinition;
import moleep.screenmate.domain.place.PlaceDefinitionRepository;
import moleep.screenmate.domain.place.UserDiscoveredPlace;
import moleep.screenmate.domain.place.UserDiscoveredPlaceRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.progress.AchievementUpsertRequest;
import moleep.screenmate.dto.progress.PlaceDiscoverRequest;
import moleep.screenmate.dto.progress.UserAchievementResponse;
import moleep.screenmate.dto.progress.UserDiscoveredPlaceResponse;
import moleep.screenmate.exception.NotFoundException;
import moleep.screenmate.validation.OwnershipValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final AchievementDefinitionRepository achievementDefinitionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final PlaceDefinitionRepository placeDefinitionRepository;
    private final UserDiscoveredPlaceRepository userDiscoveredPlaceRepository;
    private final OwnershipValidator ownershipValidator;

    @Transactional(readOnly = true)
    public List<UserAchievementResponse> getUserAchievements(User user) {
        return userAchievementRepository.findByUserIdWithDefinition(user.getId()).stream()
                .map(UserAchievementResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserAchievementResponse upsertAchievement(User user, String achievementId, AchievementUpsertRequest request) {
        AchievementDefinition definition = achievementDefinitionRepository.findById(achievementId)
                .orElseThrow(() -> new NotFoundException("ACHIEVEMENT_NOT_FOUND", "Achievement definition not found"));

        UserAchievement achievement = userAchievementRepository
                .findByUserIdAndAchievementId(user.getId(), achievementId)
                .orElseGet(() -> UserAchievement.builder()
                        .user(user)
                        .achievement(definition)
                        .build());

        if (request.getProgress() != null) {
            achievement.setProgress(request.getProgress());
        }

        if (request.getUnlockedAt() != null) {
            achievement.setUnlockedAt(request.getUnlockedAt());
        } else if (achievement.getUnlockedAt() == null && definition.getPoints() > 0 && achievement.getProgress() > 0) {
            // If client indicates progress but no timestamp, mark it now for convenience.
            achievement.setUnlockedAt(Instant.now());
        }

        if (request.getMetadata() != null) {
            achievement.setMetadata(request.getMetadata());
        }

        UserAchievement saved = userAchievementRepository.save(achievement);
        log.info("Upserted achievement {} for user {}", achievementId, user.getId());
        return UserAchievementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDiscoveredPlaceResponse> getDiscoveredPlaces(User user) {
        return userDiscoveredPlaceRepository.findByUserIdWithDefinition(user.getId()).stream()
                .map(UserDiscoveredPlaceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDiscoveredPlaceResponse discoverPlace(User user, String placeId, PlaceDiscoverRequest request) {
        PlaceDefinition definition = placeDefinitionRepository.findById(placeId)
                .orElseThrow(() -> new NotFoundException("PLACE_NOT_FOUND", "Place definition not found"));

        UserDiscoveredPlace discoveredPlace = userDiscoveredPlaceRepository
                .findByUserIdAndPlaceId(user.getId(), placeId)
                .orElseGet(() -> UserDiscoveredPlace.builder()
                        .user(user)
                        .place(definition)
                        .build());

        if (request.getDiscoveredByCharacterId() != null) {
            Character character = ownershipValidator.validateAndGetCharacter(request.getDiscoveredByCharacterId(), user);
            discoveredPlace.setDiscoveredByCharacter(character);
        }

        if (request.getMetadata() != null) {
            discoveredPlace.setMetadata(request.getMetadata());
        }

        if (discoveredPlace.getDiscoveredAt() == null) {
            discoveredPlace.setDiscoveredAt(Instant.now());
        }

        UserDiscoveredPlace saved = userDiscoveredPlaceRepository.save(discoveredPlace);
        log.info("Recorded discovered place {} for user {}", placeId, user.getId());
        return UserDiscoveredPlaceResponse.from(saved);
    }
}
