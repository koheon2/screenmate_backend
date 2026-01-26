package moleep.screenmate.dto.sync;

import lombok.Builder;
import lombok.Getter;
import moleep.screenmate.domain.memory.CharacterQaMemory;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class QaMemoryResponse {

    private UUID characterId;
    private Map<String, String> data;
    private Long version;

    public static QaMemoryResponse from(CharacterQaMemory memory) {
        return QaMemoryResponse.builder()
                .characterId(memory.getCharacter().getId())
                .data(memory.getQaData())
                .version(memory.getVersion())
                .build();
    }
}
