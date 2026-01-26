package moleep.screenmate.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CharacterFriendshipRepository extends JpaRepository<CharacterFriendship, UUID> {

    @Query("SELECT f FROM CharacterFriendship f WHERE (f.characterA.id = :characterId OR f.characterB.id = :characterId)")
    List<CharacterFriendship> findByCharacterId(@Param("characterId") UUID characterId);

    @Query("SELECT f FROM CharacterFriendship f WHERE f.characterA.id = :characterAId AND f.characterB.id = :characterBId")
    Optional<CharacterFriendship> findByCharacterPair(@Param("characterAId") UUID characterAId,
                                                      @Param("characterBId") UUID characterBId);
}
