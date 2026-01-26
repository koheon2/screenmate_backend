package moleep.screenmate.validation;

import moleep.screenmate.dto.llm.LlmGenerateResponse;
import moleep.screenmate.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ActionWhitelistValidator {

    private static final Set<String> ALLOWED_ACTION_TYPES = Set.of(
            "APPEAR_EDGE",
            "PLAY_ANIM",
            "SPEAK",
            "MOVE",
            "EMOTE",
            "SLEEP"
    );

    public void validateActions(List<LlmGenerateResponse.Action> actions) {
        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (LlmGenerateResponse.Action action : actions) {
            if (action.getType() == null) {
                throw new BadRequestException("INVALID_ACTION", "Action type cannot be null");
            }

            String actionType = action.getType().toUpperCase();
            if (!ALLOWED_ACTION_TYPES.contains(actionType)) {
                throw new BadRequestException("FORBIDDEN_ACTION_TYPE",
                        String.format("Action type '%s' is not allowed. Allowed types: %s",
                                action.getType(), ALLOWED_ACTION_TYPES));
            }
        }
    }

    public List<LlmGenerateResponse.Action> filterActions(List<LlmGenerateResponse.Action> actions) {
        if (actions == null) {
            return List.of();
        }

        return actions.stream()
                .filter(action -> action.getType() != null &&
                        ALLOWED_ACTION_TYPES.contains(action.getType().toUpperCase()))
                .toList();
    }
}
