package moleep.screenmate.dto.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "친구 메시지/이모티콘 전송")
public class FriendMessageCreateRequest {

    @Schema(description = "메시지 텍스트")
    @Size(max = 200, message = "messageText must not exceed 200 characters")
    private String messageText;

    @Schema(description = "이모티콘 ID")
    @Size(max = 50, message = "emoteId must not exceed 50 characters")
    private String emoteId;
}
