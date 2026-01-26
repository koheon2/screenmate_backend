package moleep.screenmate.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CharacterFriendRequestRepository extends JpaRepository<CharacterFriendRequest, UUID> {

    @Query("SELECT r FROM CharacterFriendRequest r WHERE r.receiverCharacter.id = :characterId AND r.status = :status")
    List<CharacterFriendRequest> findIncomingByCharacterIdAndStatus(@Param("characterId") UUID characterId,
                                                                    @Param("status") CharacterFriendRequest.Status status);

    @Query("SELECT r FROM CharacterFriendRequest r WHERE r.requesterCharacter.id = :characterId AND r.status = :status")
    List<CharacterFriendRequest> findOutgoingByCharacterIdAndStatus(@Param("characterId") UUID characterId,
                                                                    @Param("status") CharacterFriendRequest.Status status);

    @Query("SELECT r FROM CharacterFriendRequest r WHERE r.requesterCharacter.id = :requesterId AND r.receiverCharacter.id = :receiverId AND r.status = :status")
    Optional<CharacterFriendRequest> findByRequesterAndReceiverAndStatus(@Param("requesterId") UUID requesterId,
                                                                         @Param("receiverId") UUID receiverId,
                                                                         @Param("status") CharacterFriendRequest.Status status);

    @Query("SELECT r FROM CharacterFriendRequest r WHERE r.receiverCharacter.id = :receiverId AND r.id = :requestId")
    Optional<CharacterFriendRequest> findByIdAndReceiver(@Param("requestId") UUID requestId,
                                                         @Param("receiverId") UUID receiverId);
}
