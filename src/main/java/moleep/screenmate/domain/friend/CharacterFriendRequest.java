package moleep.screenmate.domain.friend;

import jakarta.persistence.*;
import lombok.*;
import moleep.screenmate.domain.character.Character;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "character_friend_requests")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CharacterFriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_character_id", nullable = false)
    private Character requesterCharacter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_character_id", nullable = false)
    private Character receiverCharacter;

    @Column(nullable = false, length = 200)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED,
        CANCELED
    }
}
