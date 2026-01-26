package moleep.screenmate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.ErrorResponse;
import moleep.screenmate.dto.llm.LlmGenerateRequest;
import moleep.screenmate.dto.llm.LlmGenerateResponse;
import moleep.screenmate.service.llm.LlmProxyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "LLM", description = "LLM 프록시 API")
@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmProxyService llmProxyService;

    @Operation(
            summary = "LLM 응답 생성 (Multipart)",
            description = """
                    스크린샷과 함께 LLM 응답을 생성합니다.

                    **보안 정책:**
                    - 스크린샷은 메모리에서만 처리되며 저장되지 않습니다
                    - 이미지 최대 크기: 5MB
                    - 지원 형식: PNG, JPEG, GIF, WebP

                    **Rate Limit:** 분당 60회

                    **허용된 액션 타입:**
                    - APPEAR_EDGE, PLAY_ANIM, SPEAK, MOVE, EMOTE, SLEEP
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = LlmGenerateResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미지 크기 초과 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate Limit 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LlmGenerateResponse> generate(
            @AuthenticationPrincipal User user,
            @Parameter(description = "캐릭터 ID", required = true)
            @RequestParam("characterId") UUID characterId,
            @Parameter(description = "사용자 메시지")
            @RequestParam(value = "userMessage", required = false) String userMessage,
            @Parameter(description = "스크린샷 이미지 (선택, 최대 5MB)")
            @RequestPart(value = "screenshot", required = false) MultipartFile screenshot) {

        LlmGenerateRequest request = LlmGenerateRequest.builder()
                .characterId(characterId)
                .userMessage(userMessage)
                .build();

        LlmGenerateResponse response = llmProxyService.generate(user, request, screenshot);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "LLM 응답 생성 (JSON)",
            description = """
                    스크린샷 없이 텍스트만으로 LLM 응답을 생성합니다.

                    **Rate Limit:** 분당 60회
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = LlmGenerateResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate Limit 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LlmGenerateResponse> generateJson(
            @AuthenticationPrincipal User user,
            @RequestBody LlmGenerateRequest request) {

        LlmGenerateResponse response = llmProxyService.generate(user, request, null);
        return ResponseEntity.ok(response);
    }
}
