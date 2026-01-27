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
import moleep.screenmate.dto.progress.AchievementUpsertRequest;
import moleep.screenmate.dto.progress.PlaceDiscoverRequest;
import moleep.screenmate.dto.progress.UserAchievementResponse;
import moleep.screenmate.dto.progress.UserDiscoveredPlaceResponse;
import moleep.screenmate.service.sync.UserProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Progress", description = "유저 업적/발견 장소 API")
@RestController
@RequiredArgsConstructor
public class UserProgressController {

    private final UserProgressService userProgressService;

    @Operation(summary = "내 업적 조회", description = "로그인한 유저의 업적 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserAchievementResponse.class)))
    })
    @GetMapping("/users/me/achievements")
    public ResponseEntity<List<UserAchievementResponse>> getMyAchievements(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userProgressService.getUserAchievements(user));
    }

    @Operation(summary = "업적 정의 조회", description = "전체 업적 정의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/users/me/achievements/definitions")
    public ResponseEntity<List<moleep.screenmate.dto.progress.AchievementDefinitionResponse>> getAchievementDefinitions(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userProgressService.getAchievementDefinitions());
    }

    @Operation(summary = "업적 진행도/해금 저장", description = "특정 업적의 진행도 또는 해금 정보를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = UserAchievementResponse.class))),
            @ApiResponse(responseCode = "404", description = "정의 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/users/me/achievements/{achievementId}")
    public ResponseEntity<UserAchievementResponse> upsertAchievement(
            @AuthenticationPrincipal User user,
            @Parameter(description = "업적 ID") @PathVariable String achievementId,
            @Valid @RequestBody AchievementUpsertRequest request) {
        return ResponseEntity.ok(userProgressService.upsertAchievement(user, achievementId, request));
    }

    @Operation(summary = "내 발견 장소 조회", description = "로그인한 유저가 발견한 장소 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDiscoveredPlaceResponse.class)))
    })
    @GetMapping("/users/me/places")
    public ResponseEntity<List<UserDiscoveredPlaceResponse>> getMyPlaces(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userProgressService.getDiscoveredPlaces(user));
    }

    @Operation(summary = "장소 정의 조회", description = "전체 장소 정의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/users/me/places/definitions")
    public ResponseEntity<List<moleep.screenmate.dto.progress.PlaceDefinitionResponse>> getPlaceDefinitions(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userProgressService.getPlaceDefinitions());
    }

    @Operation(summary = "장소 발견 기록", description = "특정 장소를 발견한 것으로 기록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기록 성공",
                    content = @Content(schema = @Schema(implementation = UserDiscoveredPlaceResponse.class))),
            @ApiResponse(responseCode = "404", description = "정의 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/users/me/places/{placeId}")
    public ResponseEntity<UserDiscoveredPlaceResponse> discoverPlace(
            @AuthenticationPrincipal User user,
            @Parameter(description = "장소 ID") @PathVariable String placeId,
            @Valid @RequestBody PlaceDiscoverRequest request) {
        return ResponseEntity.ok(userProgressService.discoverPlace(user, placeId, request));
    }
}
