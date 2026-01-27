package moleep.screenmate.dto.progress;

import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.place.PlaceDefinition;

@Getter
@Builder
public class PlaceDefinitionResponse {

    private String placeId;
    private String name;
    private String region;
    private String rarity;

    public static PlaceDefinitionResponse from(PlaceDefinition definition) {
        if (definition == null) {
            return null;
        }
        return PlaceDefinitionResponse.builder()
                .placeId(definition.getId())
                .name(definition.getName())
                .region(definition.getRegion())
                .rarity(definition.getRarity())
                .build();
    }
}
