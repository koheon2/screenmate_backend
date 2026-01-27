package moleep.screenmate.validation;

import moleep.screenmate.domain.character.Character;
import moleep.screenmate.dto.sync.CharacterPatchRequest;
import moleep.screenmate.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class CharacterValidator {

    private static final int MIN_STAT_VALUE = 0;
    private static final int MAX_STAT_VALUE = 100;
    private static final int MIN_STAGE_INDEX = 0;
    private static final int MAX_STAGE_INDEX = 3;
    private static final int MAX_EVENT_TEXT_LENGTH = 1000;

    public void validatePatch(CharacterPatchRequest patch, Character current) {
        if (patch.getHappiness() != null) {
            validateStatRange("happiness", patch.getHappiness());
        }

        if (patch.getHunger() != null) {
            validateStatRange("hunger", patch.getHunger());
        }

        if (patch.getHealth() != null) {
            validateStatRange("health", patch.getHealth());
        }

        if (patch.getIntimacyScore() != null) {
            validateStatRange("intimacyScore", patch.getIntimacyScore());
        }

        if (patch.getAggressionGauge() != null) {
            validateStatRange("aggressionGauge", patch.getAggressionGauge());
        }

        if (patch.getStageIndex() != null) {
            validateStageIndex(patch.getStageIndex(), current.getStageIndex());
        }

        if (Boolean.FALSE.equals(patch.getIsAlive()) && patch.getDiedAt() == null) {
            throw new BadRequestException("INVALID_DEATH_STATE",
                    "diedAt is required when isAlive is set to false");
        }

        if (patch.getIsAlive() != null && !current.getIsAlive() && patch.getIsAlive()) {
            throw new BadRequestException("CANNOT_REVIVE",
                    "Cannot revive a dead character");
        }
    }

    private void validateStatRange(String fieldName, Integer value) {
        if (value < MIN_STAT_VALUE || value > MAX_STAT_VALUE) {
            throw new BadRequestException("INVALID_STAT_VALUE",
                    String.format("%s must be between %d and %d", fieldName, MIN_STAT_VALUE, MAX_STAT_VALUE));
        }
    }

    private void validateStatRange(String fieldName, Double value) {
        if (value < MIN_STAT_VALUE || value > MAX_STAT_VALUE) {
            throw new BadRequestException("INVALID_STAT_VALUE",
                    String.format("%s must be between %d and %d", fieldName, MIN_STAT_VALUE, MAX_STAT_VALUE));
        }
    }

    private void validateStageIndex(Integer newStageIndex, Integer currentStageIndex) {
        if (newStageIndex < MIN_STAGE_INDEX || newStageIndex > MAX_STAGE_INDEX) {
            throw new BadRequestException("INVALID_STAGE_INDEX",
                    String.format("stageIndex must be between %d and %d", MIN_STAGE_INDEX, MAX_STAGE_INDEX));
        }

        if (newStageIndex < currentStageIndex) {
            throw new BadRequestException("STAGE_REGRESSION_NOT_ALLOWED",
                    "stageIndex cannot be decreased");
        }
    }

    public void validateEventText(String eventText) {
        if (eventText != null && eventText.length() > MAX_EVENT_TEXT_LENGTH) {
            throw new BadRequestException("EVENT_TEXT_TOO_LONG",
                    String.format("eventText must not exceed %d characters", MAX_EVENT_TEXT_LENGTH));
        }
    }

    public void validateCharacterIsAlive(Character character) {
        if (!character.getIsAlive()) {
            throw new BadRequestException("CHARACTER_IS_DEAD",
                    "Cannot perform this action on a dead character");
        }
    }
}
