package moleep.screenmate.service.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.character.CharacterRepository;
import moleep.screenmate.domain.memory.CharacterQaMemory;
import moleep.screenmate.domain.memory.CharacterQaMemoryRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.sync.CharacterCreateRequest;
import moleep.screenmate.dto.sync.CharacterPatchRequest;
import moleep.screenmate.dto.sync.CharacterResponse;
import moleep.screenmate.validation.CharacterValidator;
import moleep.screenmate.validation.OwnershipValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CharacterQaMemoryRepository qaMemoryRepository;
    private final OwnershipValidator ownershipValidator;
    private final CharacterValidator characterValidator;

    @Transactional
    public CharacterResponse createCharacter(User user, CharacterCreateRequest request) {
        Character character = Character.builder()
                .user(user)
                .name(request.getName())
                .species(request.getSpecies())
                .personality(request.getPersonality())
                .build();

        character = characterRepository.save(character);

        CharacterQaMemory qaMemory = CharacterQaMemory.builder()
                .character(character)
                .build();
        qaMemoryRepository.save(qaMemory);

        log.info("Created character: {} for user: {}", character.getId(), user.getId());

        return CharacterResponse.from(character);
    }

    @Transactional(readOnly = true)
    public List<CharacterResponse> getCharacters(User user) {
        return characterRepository.findByUserId(user.getId()).stream()
                .map(CharacterResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CharacterResponse getCharacter(UUID characterId, User user) {
        Character character = ownershipValidator.validateAndGetCharacter(characterId, user);
        return CharacterResponse.from(character);
    }

    @Transactional
    public CharacterResponse updateCharacter(UUID characterId, User user, CharacterPatchRequest request) {
        Character character = ownershipValidator.validateAndGetCharacter(characterId, user);

        characterValidator.validatePatch(request, character);

        applyPatch(character, request);

        character = characterRepository.save(character);
        log.info("Updated character: {}", character.getId());

        return CharacterResponse.from(character);
    }

    private void applyPatch(Character character, CharacterPatchRequest patch) {
        if (patch.getName() != null) {
            character.setName(patch.getName());
        }
        if (patch.getPersonality() != null) {
            character.setPersonality(patch.getPersonality());
        }
        if (patch.getStageIndex() != null) {
            character.setStageIndex(patch.getStageIndex());
        }
        if (patch.getHappiness() != null) {
            character.setHappiness(patch.getHappiness());
        }
        if (patch.getHunger() != null) {
            character.setHunger(patch.getHunger());
        }
        if (patch.getHealth() != null) {
            character.setHealth(patch.getHealth());
        }
        if (patch.getAggressionGauge() != null) {
            character.setAggressionGauge(patch.getAggressionGauge());
        }
        if (patch.getIsAlive() != null) {
            character.setIsAlive(patch.getIsAlive());
        }
        if (patch.getDiedAt() != null) {
            character.setDiedAt(patch.getDiedAt());
        }
        if (patch.getTotalPlayTimeSeconds() != null) {
            character.setTotalPlayTimeSeconds(patch.getTotalPlayTimeSeconds());
        }
        if (patch.getLastFedAt() != null) {
            character.setLastFedAt(patch.getLastFedAt());
        }
        if (patch.getLastPlayedAt() != null) {
            character.setLastPlayedAt(patch.getLastPlayedAt());
        }
    }

    @Transactional
    public void deleteCharacter(UUID characterId, User user) {
        Character character = ownershipValidator.validateAndGetCharacter(characterId, user);
        characterRepository.delete(character);
        log.info("Deleted character: {}", characterId);
    }
}
