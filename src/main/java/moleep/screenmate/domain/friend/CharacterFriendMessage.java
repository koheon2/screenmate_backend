package moleep.screenmate.domain.friend;

import jakarta.persistence.*;
import lombok.*;
import moleep.screenmate.domain.character.Character;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "character_friend_messages")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CharacterFriendMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friendship_id", nullable = false)
    private CharacterFriendship friendship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_character_id", nullable = false)
    private Character senderCharacter;

    @Column(name = "message_text", length = 200)
    private String messageText;

    @Column(name = "emote_id", length = 50)
    private String emoteId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
