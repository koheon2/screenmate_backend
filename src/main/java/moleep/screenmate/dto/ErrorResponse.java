package moleep.screenmate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "에러 발생 시각", example = "2024-01-15T10:30:00Z")
    private final Instant timestamp;

    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;

    @Schema(description = "HTTP 상태 메시지", example = "Bad Request")
    private final String error;

    @Schema(description = "에러 코드", example = "VALIDATION_ERROR")
    private final String code;

    @Schema(description = "에러 메시지", example = "Validation failed")
    private final String message;

    @Schema(description = "요청 경로", example = "/characters/123")
    private final String path;

    @Schema(description = "상세 에러 정보")
    private final Map<String, String> details;

    public static ErrorResponse of(int status, String error, String code, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .code(code)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse of(int status, String error, String code, String message, String path, Map<String, String> details) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .code(code)
                .message(message)
                .path(path)
                .details(details)
                .build();
    }
}
