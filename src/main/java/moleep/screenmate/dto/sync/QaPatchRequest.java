package moleep.screenmate.dto.sync;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "QA 메모리 패치 요청")
public class QaPatchRequest {

    @Schema(description = "예상 버전 (낙관적 락, 현재 버전과 일치해야 함)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "expectedVersion is required")
    private Long expectedVersion;

    @Schema(description = "패치할 QA 데이터 (키는 user_, pref_, fact_, memory_, context_ 접두사 필수, null 값은 삭제)",
            example = "{\"user_name\": \"홍길동\", \"pref_color\": \"파랑\", \"fact_birthday\": null}")
    private Map<String, String> qaPatch;
}
