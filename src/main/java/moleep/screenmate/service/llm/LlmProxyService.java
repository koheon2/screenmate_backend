package moleep.screenmate.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.config.OpenAiProperties;
import moleep.screenmate.config.RateLimitConfig;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.memory.CharacterQaMemory;
import moleep.screenmate.domain.memory.CharacterQaMemoryRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.llm.LlmGenerateRequest;
import moleep.screenmate.dto.llm.LlmGenerateResponse;
import moleep.screenmate.exception.BadRequestException;
import moleep.screenmate.exception.RateLimitExceededException;
import moleep.screenmate.validation.ActionWhitelistValidator;
import moleep.screenmate.validation.OwnershipValidator;
import moleep.screenmate.validation.QaMemoryValidator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProxyService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

    private final WebClient openAiWebClient;
    private final OpenAiProperties openAiProperties;
    private final RateLimitConfig rateLimitConfig;
    private final OwnershipValidator ownershipValidator;
    private final ActionWhitelistValidator actionWhitelistValidator;
    private final QaMemoryValidator qaMemoryValidator;
    private final CharacterQaMemoryRepository qaMemoryRepository;
    private final ObjectMapper objectMapper;

    public LlmGenerateResponse generate(User user, LlmGenerateRequest request, MultipartFile screenshot) {
        if (!rateLimitConfig.tryConsume(user.getId())) {
            throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
        }

        Character character = ownershipValidator.validateAndGetCharacter(request.getCharacterId(), user);

        CharacterQaMemory qaMemory = qaMemoryRepository.findByCharacterId(character.getId())
                .orElse(null);

        String systemPrompt = buildSystemPrompt(character, qaMemory);
        List<Map<String, Object>> messages = buildMessages(systemPrompt, request.getUserMessage(), screenshot);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAiProperties.getModel());
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1000);
        requestBody.put("response_format", Map.of("type", "json_object"));

        log.debug("Sending request to OpenAI for character: {}", character.getId());

        String responseJson = openAiWebClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseAndValidateResponse(responseJson);
    }

    private String buildSystemPrompt(Character character, CharacterQaMemory qaMemory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a desktop pet character named '").append(character.getName()).append("'.\n");
        prompt.append("Species: ").append(character.getSpecies()).append("\n");

        if (character.getPersonality() != null) {
            prompt.append("Personality: ").append(character.getPersonality()).append("\n");
        }

        prompt.append("Current state:\n");
        prompt.append("- Happiness: ").append(character.getHappiness()).append("/100\n");
        prompt.append("- Hunger: ").append(character.getHunger()).append("/100\n");
        prompt.append("- Health: ").append(character.getHealth()).append("/100\n");
        prompt.append("- Stage: ").append(character.getStageIndex()).append("\n");

        if (qaMemory != null && !qaMemory.getQaData().isEmpty()) {
            prompt.append("\nLearned information about the user:\n");
            qaMemory.getQaData().forEach((key, value) ->
                    prompt.append("- ").append(key).append(": ").append(value).append("\n"));
        }

        prompt.append("\nRespond in JSON format with:\n");
        prompt.append("- message: Your response text\n");
        prompt.append("- actions: Array of actions [{type, params}]\n");
        prompt.append("- qaPatch: Optional key-value pairs to remember (keys must start with: user_, pref_, fact_, memory_, context_)\n");
        prompt.append("- emotion: Your current emotion\n");
        prompt.append("\nAllowed action types: APPEAR_EDGE, PLAY_ANIM, SPEAK, MOVE, EMOTE, SLEEP\n");

        return prompt.toString();
    }

    private List<Map<String, Object>> buildMessages(String systemPrompt, String userMessage, MultipartFile screenshot) {
        List<Map<String, Object>> messages = new ArrayList<>();

        messages.add(Map.of("role", "system", "content", systemPrompt));

        List<Object> userContent = new ArrayList<>();

        if (userMessage != null && !userMessage.isBlank()) {
            userContent.add(Map.of("type", "text", "text", userMessage));
        }

        // 스크린샷이 실제로 존재하고 내용이 있는 경우에만 처리
        if (isValidScreenshot(screenshot)) {
            validateImage(screenshot);
            try {
                String base64Image = Base64.getEncoder().encodeToString(screenshot.getBytes());
                String mediaType = screenshot.getContentType() != null ? screenshot.getContentType() : "image/png";

                userContent.add(Map.of(
                        "type", "image_url",
                        "image_url", Map.of(
                                "url", "data:" + mediaType + ";base64," + base64Image,
                                "detail", "low"
                        )
                ));
                log.debug("Screenshot included in request, size: {} bytes", screenshot.getSize());
            } catch (Exception e) {
                log.error("Failed to process screenshot", e);
                throw new BadRequestException("SCREENSHOT_PROCESSING_FAILED", "Failed to process screenshot");
            }
        } else {
            log.debug("No screenshot provided, sending text-only request");
        }

        if (userContent.isEmpty()) {
            userContent.add(Map.of("type", "text", "text", "Hello"));
        }

        messages.add(Map.of("role", "user", "content", userContent));

        return messages;
    }

    /**
     * 스크린샷이 유효한지 확인 (null, empty, 0 bytes 모두 체크)
     */
    private boolean isValidScreenshot(MultipartFile screenshot) {
        if (screenshot == null) {
            return false;
        }
        if (screenshot.isEmpty()) {
            return false;
        }
        if (screenshot.getSize() <= 0) {
            return false;
        }
        // 원본 파일명도 확인 (빈 파일 파트인 경우 null일 수 있음)
        String originalFilename = screenshot.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return false;
        }
        return true;
    }

    private void validateImage(MultipartFile image) {
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("IMAGE_TOO_LARGE", "Image size must not exceed 5MB");
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("INVALID_IMAGE_TYPE",
                    "Image must be PNG, JPEG, GIF, or WebP");
        }
    }

    private LlmGenerateResponse parseAndValidateResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choices = root.path("choices");

            if (choices.isEmpty() || !choices.isArray()) {
                throw new BadRequestException("INVALID_LLM_RESPONSE", "No choices in LLM response");
            }

            String content = choices.get(0).path("message").path("content").asText();
            JsonNode contentJson = objectMapper.readTree(content);

            String message = contentJson.path("message").asText(null);
            String emotion = contentJson.path("emotion").asText(null);

            List<LlmGenerateResponse.Action> actions = new ArrayList<>();
            JsonNode actionsNode = contentJson.path("actions");
            if (actionsNode.isArray()) {
                for (JsonNode actionNode : actionsNode) {
                    String type = actionNode.path("type").asText(null);
                    Map<String, Object> params = new HashMap<>();
                    JsonNode paramsNode = actionNode.path("params");
                    if (paramsNode.isObject()) {
                        paramsNode.fields().forEachRemaining(entry ->
                                params.put(entry.getKey(), convertJsonNode(entry.getValue())));
                    }
                    actions.add(LlmGenerateResponse.Action.builder()
                            .type(type)
                            .params(params)
                            .build());
                }
            }

            List<LlmGenerateResponse.Action> filteredActions = actionWhitelistValidator.filterActions(actions);

            Map<String, String> qaPatch = new HashMap<>();
            JsonNode qaPatchNode = contentJson.path("qaPatch");
            if (qaPatchNode.isObject()) {
                qaPatchNode.fields().forEachRemaining(entry ->
                        qaPatch.put(entry.getKey(), entry.getValue().asText()));
            }

            if (!qaPatch.isEmpty()) {
                qaMemoryValidator.validateQaPatch(qaPatch);
            }

            return LlmGenerateResponse.builder()
                    .message(message)
                    .actions(filteredActions)
                    .qaPatch(qaPatch.isEmpty() ? null : qaPatch)
                    .emotion(emotion)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse LLM response", e);
            throw new BadRequestException("INVALID_LLM_RESPONSE", "Failed to parse LLM response");
        }
    }

    private Object convertJsonNode(JsonNode node) {
        if (node.isTextual()) return node.asText();
        if (node.isNumber()) return node.numberValue();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isNull()) return null;
        return node.toString();
    }
}
