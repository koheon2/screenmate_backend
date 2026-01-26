package moleep.screenmate.domain.friend;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CharacterFriendMessageRepository extends JpaRepository<CharacterFriendMessage, UUID> {

    @Query("SELECT m FROM CharacterFriendMessage m WHERE m.friendship.id = :friendshipId ORDER BY m.createdAt DESC")
    List<CharacterFriendMessage> findRecentByFriendshipId(@Param("friendshipId") UUID friendshipId, Pageable pageable);
}
