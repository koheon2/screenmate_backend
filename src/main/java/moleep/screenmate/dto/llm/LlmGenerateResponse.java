package moleep.screenmate.dto.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "LLM 생성 응답")
public class LlmGenerateResponse {

    @Schema(description = "캐릭터의 응답 메시지", example = "안녕! 오늘은 기분이 정말 좋아!")
    private String message;

    @Schema(description = "수행할 액션 목록")
    private List<Action> actions;

    @Schema(description = "저장할 QA 데이터 (키는 user_, pref_, fact_, memory_, context_ 접두사 필수)")
    private Map<String, String> qaPatch;

    @Schema(description = "현재 감정 상태", example = "happy")
    private String emotion;

    @Getter
    @Builder
    @Schema(description = "캐릭터 액션")
    public static class Action {
        @Schema(description = "액션 타입", example = "SPEAK",
                allowableValues = {"APPEAR_EDGE", "PLAY_ANIM", "SPEAK", "MOVE", "EMOTE", "SLEEP"})
        private String type;

        @Schema(description = "액션 파라미터", example = "{\"text\": \"안녕!\", \"duration\": 3}")
        private Map<String, Object> params;
    }
}
