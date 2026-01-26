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
import moleep.screenmate.dto.friend.CharacterSearchResponse;
import moleep.screenmate.dto.friend.FriendMessageCreateRequest;
import moleep.screenmate.dto.friend.FriendMessageResponse;
import moleep.screenmate.dto.friend.FriendRequestCreateRequest;
import moleep.screenmate.dto.friend.FriendRequestResponse;
import moleep.screenmate.dto.friend.FriendshipResponse;
import moleep.screenmate.service.sync.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Friend", description = "친구/초대/메시지 API")
@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "캐릭터 검색", description = "친구가 될 캐릭터를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = CharacterSearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/characters/search")
    public ResponseEntity<List<CharacterSearchResponse>> searchCharacters(
            @AuthenticationPrincipal User user,
            @Parameter(description = "검색어") @RequestParam String query,
            @Parameter(description = "최대 결과 수") @RequestParam(required = false) Integer limit) {
        List<CharacterSearchResponse> response = friendService.searchCharacters(user, query, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 요청 보내기", description = "다른 캐릭터에게 친구 요청을 보냅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 생성 성공",
                    content = @Content(schema = @Schema(implementation = FriendRequestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "요청/친구 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/characters/{id}/friend-requests")
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(
            @AuthenticationPrincipal User user,
            @Parameter(description = "요청 보내는 캐릭터 ID") @PathVariable UUID id,
            @Valid @RequestBody FriendRequestCreateRequest request) {
        FriendRequestResponse response = friendService.sendFriendRequest(id, user, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 요청 목록", description = "친구 요청 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = FriendRequestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/characters/{id}/friend-requests")
    public ResponseEntity<List<FriendRequestResponse>> getFriendRequests(
            @AuthenticationPrincipal User user,
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id,
            @Parameter(description = "incoming|outgoing") @RequestParam(required = false) String direction,
            @Parameter(description = "PENDING|ACCEPTED|REJECTED|CANCELED") @RequestParam(required = false) String status) {
        List<FriendRequestResponse> response = friendService.getFriendRequests(id, user, direction, status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 요청 수락", description = "받은 친구 요청을 수락합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수락 성공",
                    content = @Content(schema = @Schema(implementation = FriendRequestResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "상태 충돌",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/characters/{id}/friend-requests/{requestId}/accept")
    public ResponseEntity<FriendRequestResponse> acceptFriendRequest(
            @AuthenticationPrincipal User user,
            @Parameter(description = "수락하는 캐릭터 ID") @PathVariable UUID id,
            @Parameter(description = "친구 요청 ID") @PathVariable UUID requestId) {
        FriendRequestResponse response = friendService.acceptFriendRequest(id, user, requestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공",
                    content = @Content(schema = @Schema(implementation = FriendRequestResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "상태 충돌",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/characters/{id}/friend-requests/{requestId}/reject")
    public ResponseEntity<FriendRequestResponse> rejectFriendRequest(
            @AuthenticationPrincipal User user,
            @Parameter(description = "거절하는 캐릭터 ID") @PathVariable UUID id,
            @Parameter(description = "친구 요청 ID") @PathVariable UUID requestId) {
        FriendRequestResponse response = friendService.rejectFriendRequest(id, user, requestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 목록 조회", description = "친구 목록과 친밀도를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = FriendshipResponse.class)))
    })
    @GetMapping("/characters/{id}/friends")
    public ResponseEntity<List<FriendshipResponse>> getFriends(
            @AuthenticationPrincipal User user,
            @Parameter(description = "캐릭터 ID") @PathVariable UUID id) {
        List<FriendshipResponse> response = friendService.getFriends(id, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구에게 메시지/이모티콘 보내기", description = "친구에게 메시지 또는 이모티콘을 보냅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전송 성공",
                    content = @Content(schema = @Schema(implementation = FriendMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "친구 관계 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/characters/{id}/friends/{friendId}/messages")
    public ResponseEntity<FriendMessageResponse> sendFriendMessage(
            @AuthenticationPrincipal User user,
            @Parameter(description = "내 캐릭터 ID") @PathVariable UUID id,
            @Parameter(description = "친구 캐릭터 ID") @PathVariable UUID friendId,
            @Valid @RequestBody FriendMessageCreateRequest request) {
        FriendMessageResponse response = friendService.sendFriendMessage(id, friendId, user, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 메시지 조회", description = "친구와 주고받은 메시지를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = FriendMessageResponse.class)))
    })
    @GetMapping("/characters/{id}/friends/{friendId}/messages")
    public ResponseEntity<List<FriendMessageResponse>> getFriendMessages(
            @AuthenticationPrincipal User user,
            @Parameter(description = "내 캐릭터 ID") @PathVariable UUID id,
            @Parameter(description = "친구 캐릭터 ID") @PathVariable UUID friendId,
            @Parameter(description = "최대 조회 개수") @RequestParam(required = false) Integer limit) {
        List<FriendMessageResponse> response = friendService.getFriendMessages(id, friendId, user, limit);
        return ResponseEntity.ok(response);
    }
}
