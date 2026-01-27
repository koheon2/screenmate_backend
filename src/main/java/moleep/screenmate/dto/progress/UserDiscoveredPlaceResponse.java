package moleep.screenmate.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.place.UserDiscoveredPlace;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "유저 발견 장소 응답")
public class UserDiscoveredPlaceResponse {

    private UUID id;
    private String placeId;
    private String name;
    private String region;
    private String rarity;
    private Instant discoveredAt;
    private UUID discoveredByCharacterId;
    private Map<String, Object> metadata;

    public static UserDiscoveredPlaceResponse from(UserDiscoveredPlace udp) {
        return UserDiscoveredPlaceResponse.builder()
                .id(udp.getId())
                .placeId(udp.getPlace().getId())
                .name(udp.getPlace().getName())
                .region(udp.getPlace().getRegion())
                .rarity(udp.getPlace().getRarity())
                .discoveredAt(udp.getDiscoveredAt())
                .discoveredByCharacterId(udp.getDiscoveredByCharacter() != null ? udp.getDiscoveredByCharacter().getId() : null)
                .metadata(udp.getMetadata())
                .build();
    }
}
