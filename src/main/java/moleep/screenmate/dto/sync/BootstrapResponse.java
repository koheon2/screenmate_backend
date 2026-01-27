package moleep.screenmate.dto.sync;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class BootstrapResponse {

    private UserData user;
    private List<CharacterData> characters;
    private List<AchievementData> achievements;
    private List<DiscoveredPlaceData> discoveredPlaces;

    @Getter
    @Builder
    public static class UserData {
        private UUID id;
        private String email;
        private String displayName;
        private String profileImageUrl;
        private Instant createdAt;
        private Instant lastLoginAt;
    }

    @Getter
    @Builder
    public static class CharacterData {
        private UUID id;
        private String name;
        private String species;
        private String inviteCode;
        private String homePlaceId;
        private String personality;
        private Integer stageIndex;
        private Integer happiness;
        private Integer hunger;
        private Integer health;
        private Integer aggressionGauge;
        private Boolean isAlive;
        private Instant diedAt;
        private Long totalPlayTimeSeconds;
        private Instant lastFedAt;
        private Instant lastPlayedAt;
        private Instant createdAt;
        private Instant updatedAt;
        private Long version;
        private QaMemoryData qaMemory;
        private List<EventData> recentEvents;
    }

    @Getter
    @Builder
    public static class QaMemoryData {
        private Map<String, String> data;
        private Long version;
    }

    @Getter
    @Builder
    public static class EventData {
        private UUID id;
        private String eventType;
        private String eventText;
        private Instant createdAt;
    }

    @Getter
    @Builder
    public static class AchievementData {
        private UUID id;
        private String achievementId;
        private String name;
        private String description;
        private String category;
        private Integer points;
        private Boolean hidden;
        private Integer progress;
        private Instant unlockedAt;
        private Map<String, Object> metadata;
    }

    @Getter
    @Builder
    public static class DiscoveredPlaceData {
        private UUID id;
        private String placeId;
        private String name;
        private String region;
        private String rarity;
        private Instant discoveredAt;
        private UUID discoveredByCharacterId;
        private Map<String, Object> metadata;
    }
}
