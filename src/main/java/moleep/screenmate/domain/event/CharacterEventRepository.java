package moleep.screenmate.domain.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CharacterEventRepository extends JpaRepository<CharacterEvent, UUID> {

    List<CharacterEvent> findByCharacterId(UUID characterId);

    Page<CharacterEvent> findByCharacterIdOrderByCreatedAtDesc(UUID characterId, Pageable pageable);

    @Query("SELECT e FROM CharacterEvent e WHERE e.character.id = :characterId AND e.character.user.id = :userId ORDER BY e.createdAt DESC")
    List<CharacterEvent> findByCharacterIdAndUserId(@Param("characterId") UUID characterId, @Param("userId") UUID userId);

    @Query("SELECT e FROM CharacterEvent e WHERE e.character.id = :characterId AND e.createdAt > :since ORDER BY e.createdAt DESC")
    List<CharacterEvent> findByCharacterIdSince(@Param("characterId") UUID characterId, @Param("since") Instant since);

    @Query("SELECT e FROM CharacterEvent e WHERE e.character.id = :characterId AND e.eventType = :eventType ORDER BY e.createdAt DESC")
    List<CharacterEvent> findByCharacterIdAndEventType(@Param("characterId") UUID characterId, @Param("eventType") CharacterEvent.EventType eventType);
}
