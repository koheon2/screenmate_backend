package moleep.screenmate.dto.sync;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "캐릭터 생성 요청")
public class CharacterCreateRequest {

    @Schema(description = "캐릭터 이름", example = "뽀삐", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must not exceed 50 characters")
    private String name;

    @Schema(description = "종족", example = "고양이", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
    @NotBlank(message = "species is required")
    @Size(max = 50, message = "species must not exceed 50 characters")
    private String species;

    @Schema(description = "성격 설명", example = "장난꾸러기이고 호기심이 많음", maxLength = 500)
    @Size(max = 500, message = "personality must not exceed 500 characters")
    private String personality;
}
