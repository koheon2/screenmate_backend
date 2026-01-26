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
import moleep.screenmate.dto.sync.*;
import moleep.screenmate.service.sync.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SyncController {

    private final BootstrapService bootstrapService;
    private final CharacterService characterService;
    private final QaMemoryService qaMemoryService;
    private final EventService eventService;

    @Tag(name = "Sync", description = "데이터 동기화 API")
    @Operation(summary = "부트스트랩 데이터 조회", description = "앱 시작 시 필요한 모든 사용자 데이터를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BootstrapResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/sync/bootstrap")
    public ResponseEntity<BootstrapResponse> getBootstrapData(@AuthenticationPrincipal User user) {
        BootstrapResponse response = bootstrapService.getBootstrapData(user);
        return ResponseEntity.ok(response);
    }

    @Tag(name = "Character", description = "캐릭터 관리 API")
    @Operation(summary = "캐릭터 생성", description = "새로운 캐릭터를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = CharacterResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/characters")
    public ResponseEntity<CharacterResponse> createCharacter(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CharacterCreateRequest request) {
        CharacterResponse response = characterService.createCharacter(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Tag(name = "Character")
    @Operation(summary = "캐릭터 목록 조회", description = "사용자의 모든 캐릭터를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/characters")
    public ResponseEntity<List<CharacterResponse>> getCharacters(@AuthenticationPrincipal User user) {
        List<CharacterResponse> response = characterService.getCharacters(user);
        return ResponseEntity.ok(response);
    }

    @Tag(name = "Character")
    @Operation(summary = "캐릭터 상세 조회", description = "특정 캐릭터의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CharacterResponse.class))),
            @ApiResponse(responseCode = "404", description = "캐릭터를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/characters/{id}")
    public ResponseEntity<CharacterResponse> getCharacter(
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        CharacterResponse response = characterService.getCharacter(id, user);
        return ResponseEntity.ok(response);
    }

    @Tag(name = "Character")
    @Operation(summary = "캐릭터 상태 업데이트", description = "캐릭터의 상태를 부분 업데이트합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공",
                    content = @Content(schema = @Schema(implementation = CharacterResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (예: 스탯 범위 초과)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "캐릭터를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/characters/{id}")
    public ResponseEntity<CharacterResponse> updateCharacter(
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @RequestBody CharacterPatchRequest request) {
        CharacterResponse response = characterService.updateCharacter(id, user, request);
        return ResponseEntity.ok(response);
    }

    @Tag(name = "Character")
    @Operation(summary = "캐릭터 삭제", description = "캐릭터를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "캐릭터를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/characters/{id}")
    public ResponseEntity<Void> deleteCharacter(
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        characterService.deleteCharacter(id, user);
        return ResponseEntity.noContent().build();
    }

    @Tag(name = "QA Memory", description = "캐릭터 QA 메모리 관리 API")
    @Operation(summary = "QA 메모리 조회", description = "캐릭터의 QA 메모리를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = QaMemoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "캐릭터 또는 메모리를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/characters/{id}/qa")
    public ResponseEntity<QaMemoryResponse> getQaMemory(
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        QaMemoryResponse response = qaMemoryService.getQaMemory(id, user);
        return ResponseEntity.ok(response);
    }

    @Tag(name = "QA Memory")
    @Operation(summary = "QA 메모리 패치", description = "캐릭터의 QA 메모리를 부분 업데이트합니다. 낙관적 락을 사용하며, 버전이 불일치하면 409 Conflict를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공",
                    content = @Content(schema = @Schema(implementation = QaMemoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (예: 잘못된 키 접두사)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "버전 충돌",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/characters/{id}/qa")
    public ResponseEntity<QaMemoryResponse> patchQaMemory(
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody QaPatchRequest request) {
        QaMemoryResponse response = qaMemoryService.patchQaMemory(id, user, request);
        return ResponseEntity.ok(response);
    }

    @Tag(name = "Event", description = "캐릭터 이벤트 관리 API")
    @Operation(summary = "이벤트 생성", description = "캐릭터의 새 이벤트를 기록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/characters/{id}/events")
    public ResponseEntity<EventResponse> createEvent(
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody EventCreateRequest request) {
        EventResponse response = eventService.createEvent(id, user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Tag(name = "Event")
    @Operation(summary = "이벤트 목록 조회", description = "캐릭터의 이벤트 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/characters/{id}/events")
    public ResponseEntity<List<EventResponse>> getEvents(
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @Parameter(description = "최대 조회 개수 (최대 100)") @RequestParam(defaultValue = "20") int limit) {
        List<EventResponse> response = eventService.getEvents(id, user, Math.min(limit, 100));
        return ResponseEntity.ok(response);
    }
}
