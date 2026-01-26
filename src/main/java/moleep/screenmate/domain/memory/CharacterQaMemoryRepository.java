package moleep.screenmate.domain.memory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CharacterQaMemoryRepository extends JpaRepository<CharacterQaMemory, UUID> {

    Optional<CharacterQaMemory> findByCharacterId(UUID characterId);

    @Query("SELECT m FROM CharacterQaMemory m WHERE m.character.id = :characterId AND m.character.user.id = :userId")
    Optional<CharacterQaMemory> findByCharacterIdAndUserId(@Param("characterId") UUID characterId, @Param("userId") UUID userId);

    boolean existsByCharacterId(UUID characterId);
}
