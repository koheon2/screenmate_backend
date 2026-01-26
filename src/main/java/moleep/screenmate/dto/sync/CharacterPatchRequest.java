package moleep.screenmate.dto.sync;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "캐릭터 상태 업데이트 요청 (부분 업데이트)")
public class CharacterPatchRequest {

    @Schema(description = "캐릭터 이름", example = "뽀삐")
    private String name;

    @Schema(description = "성격 설명", example = "장난꾸러기이고 호기심이 많음")
    private String personality;

    @Schema(description = "진화 단계 (0-3, 감소 불가)", example = "1", minimum = "0", maximum = "3")
    private Integer stageIndex;

    @Schema(description = "행복도 (0-100)", example = "75", minimum = "0", maximum = "100")
    private Integer happiness;

    @Schema(description = "배고픔 (0-100)", example = "30", minimum = "0", maximum = "100")
    private Integer hunger;

    @Schema(description = "건강 (0-100)", example = "100", minimum = "0", maximum = "100")
    private Integer health;

    @Schema(description = "공격성 게이지 (0-100)", example = "10", minimum = "0", maximum = "100")
    private Integer aggressionGauge;

    @Schema(description = "생존 여부 (false 설정 시 diedAt 필수)", example = "true")
    private Boolean isAlive;

    @Schema(description = "사망 시각 (isAlive=false일 때 필수)")
    private Instant diedAt;

    @Schema(description = "총 플레이 시간 (초)", example = "3600")
    private Long totalPlayTimeSeconds;

    @Schema(description = "마지막 먹이 시각")
    private Instant lastFedAt;

    @Schema(description = "마지막 놀이 시각")
    private Instant lastPlayedAt;
}
