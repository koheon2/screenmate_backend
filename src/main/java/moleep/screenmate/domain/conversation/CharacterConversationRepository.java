package moleep.screenmate.domain.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CharacterConversationRepository extends JpaRepository<CharacterConversation, UUID> {

    List<CharacterConversation> findTop20ByCharacterIdOrderByCreatedAtDesc(UUID characterId);

    long countByCharacterId(UUID characterId);
}

