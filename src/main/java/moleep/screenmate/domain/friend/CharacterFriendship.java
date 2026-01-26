package moleep.screenmate.domain.friend;

import jakarta.persistence.*;
import lombok.*;
import moleep.screenmate.domain.character.Character;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "character_friendships")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CharacterFriendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_a_id", nullable = false)
    private Character characterA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_b_id", nullable = false)
    private Character characterB;

    @Column(nullable = false)
    @Builder.Default
    private Integer intimacy = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
