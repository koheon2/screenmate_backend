package moleep.screenmate.domain.lineage;

import moleep.screenmate.domain.character.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CharacterLineageRepository extends JpaRepository<CharacterLineage, UUID> {

    boolean existsByChildCharacterId(UUID childCharacterId);

    Optional<CharacterLineage> findByChildCharacterId(UUID childCharacterId);

    @Query("select l from CharacterLineage l where l.childCharacter.id in :ids")
    List<CharacterLineage> findByChildCharacterIds(@Param("ids") Collection<UUID> ids);

    @Query("""
            select l from CharacterLineage l
            where l.parentACharacter.id in :ids or l.parentBCharacter.id in :ids
            """)
    List<CharacterLineage> findByParentCharacterIds(@Param("ids") Collection<UUID> ids);

    default List<CharacterLineage> findConnectedEdges(Collection<UUID> ids) {
        List<CharacterLineage> byChild = findByChildCharacterIds(ids);
        List<CharacterLineage> byParent = findByParentCharacterIds(ids);
        byChild.addAll(byParent);
        return byChild;
    }
}

