package moleep.screenmate.dto.progress;

import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.achievement.AchievementDefinition;

@Getter
@Builder
public class AchievementDefinitionResponse {

    private String achievementId;
    private String name;
    private String description;
    private String category;
    private Integer points;
    private Boolean hidden;

    public static AchievementDefinitionResponse from(AchievementDefinition definition) {
        if (definition == null) {
            return null;
        }
        return AchievementDefinitionResponse.builder()
                .achievementId(definition.getId())
                .name(definition.getName())
                .description(definition.getDescription())
                .category(definition.getCategory())
                .points(definition.getPoints())
                .hidden(definition.getHidden())
                .build();
    }
}
