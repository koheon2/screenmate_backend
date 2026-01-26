package moleep.screenmate.validation;

import lombok.RequiredArgsConstructor;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.character.CharacterRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.exception.ForbiddenException;
import moleep.screenmate.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OwnershipValidator {

    private final CharacterRepository characterRepository;

    public Character validateAndGetCharacter(UUID characterId, User user) {
        return characterRepository.findByIdAndUserId(characterId, user.getId())
                .orElseThrow(() -> new NotFoundException("CHARACTER_NOT_FOUND",
                        "Character not found or you don't have access"));
    }

    public void validateOwnership(UUID characterId, User user) {
        if (!characterRepository.existsByIdAndUserId(characterId, user.getId())) {
            throw new ForbiddenException("ACCESS_DENIED",
                    "You don't have permission to access this character");
        }
    }

    public void validateCharacterExists(UUID characterId) {
        if (!characterRepository.existsById(characterId)) {
            throw new NotFoundException("CHARACTER_NOT_FOUND", "Character not found");
        }
    }
}
