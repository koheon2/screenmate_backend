package moleep.screenmate.domain.character;

import jakarta.persistence.*;
import lombok.*;
import moleep.screenmate.domain.user.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "species", nullable = false)
    private String species;

    @Column(name = "invite_code", nullable = false, unique = true, length = 12)
    private String inviteCode;

    @Column(name = "home_place_id", nullable = false, length = 100)
    @Builder.Default
    private String homePlaceId = "house1";

    @Column(name = "personality")
    private String personality;

    @Column(name = "stage_index", nullable = false)
    @Builder.Default
    private Integer stageIndex = 0;

    @Column(name = "intimacy_score", nullable = false)
    @Builder.Default
    private Double intimacyScore = 0.0;

    @Column(name = "intimacy_daily_date")
    private LocalDate intimacyDailyDate;

    @Column(name = "intimacy_daily_count", nullable = false)
    @Builder.Default
    private Integer intimacyDailyCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer happiness = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer hunger = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer health = 100;

    @Column(name = "aggression_gauge", nullable = false)
    @Builder.Default
    private Integer aggressionGauge = 0;

    @Column(name = "is_alive", nullable = false)
    @Builder.Default
    private Boolean isAlive = true;

    @Column(name = "died_at")
    private Instant diedAt;

    @Column(name = "total_play_time_seconds", nullable = false)
    @Builder.Default
    private Long totalPlayTimeSeconds = 0L;

    @Column(name = "last_fed_at")
    private Instant lastFedAt;

    @Column(name = "last_played_at")
    private Instant lastPlayedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public void die() {
        this.isAlive = false;
        this.diedAt = Instant.now();
    }

    public void evolve() {
        if (this.stageIndex < 3) {
            this.stageIndex++;
        }
    }
}
