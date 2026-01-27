package moleep.screenmate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.ErrorResponse;
import moleep.screenmate.dto.lineage.LineageCreateRequest;
import moleep.screenmate.dto.lineage.LineageEdgeResponse;
import moleep.screenmate.dto.lineage.LineageGraphResponse;
import moleep.screenmate.service.sync.LineageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Lineage", description = "가계도(부모-자식) 그래프 API")
@RestController
@RequiredArgsConstructor
public class LineageController {

    private final LineageService lineageService;

    @Operation(summary = "가계도 관계 생성", description = "자식 캐릭터 기준으로 부모 관계를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = LineageEdgeResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/characters/{id}/lineage")
    public ResponseEntity<LineageEdgeResponse> createLineage(
            @AuthenticationPrincipal User user,
            @Parameter(description = "자식 캐릭터 ID") @PathVariable UUID id,
            @Valid @RequestBody LineageCreateRequest request) {
        LineageEdgeResponse response = lineageService.createLineage(id, user, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "가계도 조회", description = "특정 캐릭터를 중심으로 가계도 그래프를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = LineageGraphResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/characters/{id}/lineage")
    public ResponseEntity<LineageGraphResponse> getLineage(
            @AuthenticationPrincipal User user,
            @Parameter(description = "루트 캐릭터 ID") @PathVariable UUID id,
            @Parameter(description = "탐색 깊이 (기본 3)") @RequestParam(required = false) Integer depth) {
        LineageGraphResponse response = lineageService.getLineageGraph(id, user, depth);
        return ResponseEntity.ok(response);
    }
}

