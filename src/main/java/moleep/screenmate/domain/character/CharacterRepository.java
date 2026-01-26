package moleep.screenmate.domain.character;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CharacterRepository extends JpaRepository<Character, UUID> {

    List<Character> findByUserId(UUID userId);

    @Query("SELECT c FROM Character c WHERE c.id = :characterId AND c.user.id = :userId")
    Optional<Character> findByIdAndUserId(@Param("characterId") UUID characterId, @Param("userId") UUID userId);

    @Query("SELECT c FROM Character c WHERE c.user.id = :userId AND c.isAlive = true")
    List<Character> findAliveCharactersByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(c) FROM Character c WHERE c.user.id = :userId AND c.isAlive = true")
    long countAliveCharactersByUserId(@Param("userId") UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
