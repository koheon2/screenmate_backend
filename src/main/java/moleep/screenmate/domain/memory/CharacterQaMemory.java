package moleep.screenmate.domain.memory;

import jakarta.persistence.*;
import lombok.*;
import moleep.screenmate.domain.character.Character;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "character_qa_memories")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CharacterQaMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, unique = true)
    private Character character;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "qa_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> qaData = new HashMap<>();

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void mergeQaData(Map<String, String> patch) {
        if (patch != null) {
            this.qaData.putAll(patch);
        }
    }

    public void removeKey(String key) {
        this.qaData.remove(key);
    }
}
