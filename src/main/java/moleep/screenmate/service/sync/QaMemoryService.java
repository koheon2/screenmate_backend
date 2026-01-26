package moleep.screenmate.service.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.memory.CharacterQaMemory;
import moleep.screenmate.domain.memory.CharacterQaMemoryRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.sync.QaMemoryResponse;
import moleep.screenmate.dto.sync.QaPatchRequest;
import moleep.screenmate.exception.NotFoundException;
import moleep.screenmate.exception.VersionConflictException;
import moleep.screenmate.validation.OwnershipValidator;
import moleep.screenmate.validation.QaMemoryValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaMemoryService {

    private final CharacterQaMemoryRepository qaMemoryRepository;
    private final OwnershipValidator ownershipValidator;
    private final QaMemoryValidator qaMemoryValidator;

    @Transactional(readOnly = true)
    public QaMemoryResponse getQaMemory(UUID characterId, User user) {
        ownershipValidator.validateOwnership(characterId, user);

        CharacterQaMemory memory = qaMemoryRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new NotFoundException("QA_MEMORY_NOT_FOUND", "QA memory not found for character"));

        return QaMemoryResponse.from(memory);
    }

    @Transactional
    public QaMemoryResponse patchQaMemory(UUID characterId, User user, QaPatchRequest request) {
        Character character = ownershipValidator.validateAndGetCharacter(characterId, user);

        qaMemoryValidator.validateQaPatch(request.getQaPatch());

        CharacterQaMemory memory = qaMemoryRepository.findByCharacterId(characterId)
                .orElseGet(() -> {
                    CharacterQaMemory newMemory = CharacterQaMemory.builder()
                            .character(character)
                            .build();
                    return qaMemoryRepository.save(newMemory);
                });

        if (!request.getExpectedVersion().equals(memory.getVersion())) {
            throw new VersionConflictException(request.getExpectedVersion(), memory.getVersion());
        }

        if (request.getQaPatch() != null) {
            request.getQaPatch().forEach((key, value) -> {
                if (value == null) {
                    memory.removeKey(key);
                } else {
                    memory.getQaData().put(key, value);
                }
            });
        }

        CharacterQaMemory savedMemory = qaMemoryRepository.save(memory);
        log.info("Updated QA memory for character: {}, new version: {}", characterId, savedMemory.getVersion());

        return QaMemoryResponse.from(savedMemory);
    }
}
