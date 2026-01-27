package moleep.screenmate.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.achievement.UserAchievement;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "유저 업적 응답")
public class UserAchievementResponse {

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

    public static UserAchievementResponse from(UserAchievement ua) {
        return UserAchievementResponse.builder()
                .id(ua.getId())
                .achievementId(ua.getAchievement().getId())
                .name(ua.getAchievement().getName())
                .description(ua.getAchievement().getDescription())
                .category(ua.getAchievement().getCategory())
                .points(ua.getAchievement().getPoints())
                .hidden(ua.getAchievement().getHidden())
                .progress(ua.getProgress())
                .unlockedAt(ua.getUnlockedAt())
                .metadata(ua.getMetadata())
                .build();
    }
}
