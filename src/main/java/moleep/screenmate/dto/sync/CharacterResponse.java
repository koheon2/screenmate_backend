package moleep.screenmate.dto.sync;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.character.Character;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "캐릭터 응답")
public class CharacterResponse {

    @Schema(description = "캐릭터 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "캐릭터 이름", example = "뽀삐")
    private String name;

    @Schema(description = "종족", example = "고양이")
    private String species;

    @Schema(description = "초대 코드", example = "H7K9Q2LM")
    private String inviteCode;

    @Schema(description = "집 장소 ID", example = "house4")
    private String homePlaceId;

    @Schema(description = "성격 설명", example = "장난꾸러기이고 호기심이 많음")
    private String personality;

    @Schema(description = "진화 단계 (0-3)", example = "1")
    private Integer stageIndex;

    @Schema(description = "유저와의 친밀도 (0-100)", example = "12.4")
    private Double intimacyScore;

    @Schema(description = "행복도 (0-100)", example = "75")
    private Integer happiness;

    @Schema(description = "배고픔 (0-100)", example = "30")
    private Integer hunger;

    @Schema(description = "건강 (0-100)", example = "100")
    private Integer health;

    @Schema(description = "공격성 게이지 (0-100)", example = "10")
    private Integer aggressionGauge;

    @Schema(description = "생존 여부", example = "true")
    private Boolean isAlive;

    @Schema(description = "사망 시각")
    private Instant diedAt;

    @Schema(description = "총 플레이 시간 (초)", example = "3600")
    private Long totalPlayTimeSeconds;

    @Schema(description = "마지막 먹이 시각")
    private Instant lastFedAt;

    @Schema(description = "마지막 놀이 시각")
    private Instant lastPlayedAt;

    @Schema(description = "생성 시각")
    private Instant createdAt;

    @Schema(description = "수정 시각")
    private Instant updatedAt;

    @Schema(description = "버전 (낙관적 락용)", example = "1")
    private Long version;

    public static CharacterResponse from(Character character) {
        return CharacterResponse.builder()
                .id(character.getId())
                .name(character.getName())
                .species(character.getSpecies())
                .inviteCode(character.getInviteCode())
                .homePlaceId(character.getHomePlaceId())
                .personality(character.getPersonality())
                .stageIndex(character.getStageIndex())
                .intimacyScore(character.getIntimacyScore())
                .happiness(character.getHappiness())
                .hunger(character.getHunger())
                .health(character.getHealth())
                .aggressionGauge(character.getAggressionGauge())
                .isAlive(character.getIsAlive())
                .diedAt(character.getDiedAt())
                .totalPlayTimeSeconds(character.getTotalPlayTimeSeconds())
                .lastFedAt(character.getLastFedAt())
                .lastPlayedAt(character.getLastPlayedAt())
                .createdAt(character.getCreatedAt())
                .updatedAt(character.getUpdatedAt())
                .version(character.getVersion())
                .build();
    }
}
