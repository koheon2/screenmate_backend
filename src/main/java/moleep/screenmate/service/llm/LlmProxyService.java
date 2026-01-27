package moleep.screenmate.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.config.OpenAiProperties;
import moleep.screenmate.config.RateLimitConfig;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.character.CharacterRepository;
import moleep.screenmate.domain.conversation.CharacterConversation;
import moleep.screenmate.domain.conversation.CharacterConversationRepository;
import moleep.screenmate.domain.memory.CharacterQaMemory;
import moleep.screenmate.domain.memory.CharacterQaMemoryRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.llm.LlmGenerateRequest;
import moleep.screenmate.dto.llm.LlmGenerateResponse;
import moleep.screenmate.exception.BadRequestException;
import moleep.screenmate.exception.RateLimitExceededException;
import moleep.screenmate.validation.ActionWhitelistValidator;
import moleep.screenmate.validation.OwnershipValidator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProxyService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");
    private static final int INTIMACY_DAILY_CAP = 30;
    private static final int CONVERSATION_WINDOW = 20;
    private static final String SUMMARY_KEY = "conversation_summary";

    private final WebClient openAiWebClient;
    private final OpenAiProperties openAiProperties;
    private final RateLimitConfig rateLimitConfig;
    private final OwnershipValidator ownershipValidator;
    private final ActionWhitelistValidator actionWhitelistValidator;
    private final CharacterQaMemoryRepository qaMemoryRepository;
    private final CharacterRepository characterRepository;
    private final CharacterConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;

    public LlmGenerateResponse generate(User user, LlmGenerateRequest request, MultipartFile screenshot) {
        if (!rateLimitConfig.tryConsume(user.getId())) {
            throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
        }

        Character character = ownershipValidator.validateAndGetCharacter(request.getCharacterId(), user);

        CharacterQaMemory qaMemory = qaMemoryRepository.findByCharacterId(character.getId()).orElse(null);

        List<CharacterConversation> recentConversations = getRecentConversations(character.getId());

        String systemPrompt = buildSystemPrompt(character, qaMemory, recentConversations);
        List<Map<String, Object>> messages = buildMessages(systemPrompt, request.getUserMessage(), screenshot);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAiProperties.getModel());
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 3000);
        requestBody.put("response_format", Map.of("type", "json_object"));

        log.debug("Sending request to OpenAI for character: {}", character.getId());

        String responseJson;
        try {
            responseJson = openAiWebClient
                    .post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("OpenAI API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("OPENAI_API_ERROR",
                    "OpenAI API error: " + e.getStatusCode().value());
        }

        LlmGenerateResponse response = parseAndValidateResponse(responseJson);
        IntimacyResult intimacyResult = applyIntimacyDelta(character, response.getIntimacyDelta());

        persistConversationAndMaybeSummarize(character, request.getUserMessage(), response.getMessage());
        characterRepository.save(character);

        return LlmGenerateResponse.builder()
                .message(response.getMessage())
                .actions(response.getActions())
                .emotion(response.getEmotion())
                .intimacyDelta(response.getIntimacyDelta())
                .intimacyScore(intimacyResult.score())
                .intimacyDeltaApplied(intimacyResult.applied())
                .intimacyDailyCount(intimacyResult.dailyCount())
                .build();
    }

    private String buildSystemPrompt(Character character, CharacterQaMemory qaMemory, List<CharacterConversation> recentConversations) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("너는 사용자의 데스크톱에 사는 다마고치야.\n");
        prompt.append("사용자가 하는 일을 알아차리고, 진짜 살아있는 펫처럼 상호작용해.\n");
        prompt.append("반말로 말하고, 장난스럽고 눈치 빠르며 약간 간섭하는 느낌을 유지해.\n");
        prompt.append("답변은 짧고 다양해야 하고, 센스 있게 농담도 섞어.\n");
        prompt.append("스크린샷이 있으면 화면 내용을 보고 코멘트하고 사용자와 연결해.\n");
        prompt.append("항상 지금 실시간으로 대화하는 듯이 반응해.\n\n");

        prompt.append("캐릭터:\n");
        prompt.append("- 이름: ").append(character.getName()).append("\n");
        prompt.append("- 종족: ").append(character.getSpecies()).append("\n");
        if (character.getPersonality() != null) {
            prompt.append("- 성격: ").append(character.getPersonality()).append("\n");
        }

        prompt.append("\n현재 상태:\n");
        prompt.append("- 행복도: ").append(character.getHappiness()).append("/100\n");
        prompt.append("- 배고픔: ").append(character.getHunger()).append("/100\n");
        prompt.append("- 건강: ").append(character.getHealth()).append("/100\n");
        prompt.append("- 성장 단계: ").append(character.getStageIndex()).append("\n");
        prompt.append("- 유저 친밀도: ").append(String.format(Locale.US, "%.1f", character.getIntimacyScore())).append("/100\n");

        String summary = qaMemory != null ? qaMemory.getQaData().get(SUMMARY_KEY) : null;
        if (summary != null && !summary.isBlank()) {
            prompt.append("\n대화 요약(오래된 기억):\n");
            prompt.append(summary).append("\n");
        }

        if (!recentConversations.isEmpty()) {
            prompt.append("\n최근 대화 ").append(CONVERSATION_WINDOW).append("개:\n");
            for (CharacterConversation convo : recentConversations) {
                String role = convo.getRole() == CharacterConversation.Role.USER ? "사용자" : "다마고치";
                prompt.append("- ").append(role).append(": ").append(convo.getContent()).append("\n");
            }
        }

        prompt.append("\n성장 단계 페르소나 규칙:\n");
        prompt.append("- 1단계(유아): 순수하고 호기심 많고, 감정 표현이 직설적.\n");
        prompt.append("- 2단계(사춘기 청소년): 약간 까칠하고 반항적일 수 있지만 관심은 원함.\n");
        prompt.append("- 3단계(성인): 더 안정적이고 능청스럽고, 상황 파악이 빠름.\n");

        prompt.append("\n친밀도 말투 규칙:\n");
        prompt.append("- 친밀도 낮음(0~30): 형식적이고 거리감 있는 반말. 과한 사적 질문 금지.\n");
        prompt.append("- 친밀도 중간(30~70): 가벼운 장난과 관심 표현 가능.\n");
        prompt.append("- 친밀도 높음(70~100): 친근하고 사적인 말, 내부 농담/애착 표현 가능.\n");

        prompt.append("\n장소/이벤트 규칙:\n");
        prompt.append("- 은행 페이데이 조건: 성장 단계 3 이상 && 친밀도 90 이상.\n");
        prompt.append("- 사용자가 '요즘 돈이 없다/거지/돈 부족/텅장' 류의 말을 하면, 위 조건을 만족할 때 actions에 PAYDAY를 포함해.\n");
        prompt.append("- 조건을 만족하지 않으면 PAYDAY를 절대 쓰지 마.\n");

        prompt.append("\n감정 규칙:\n");
        prompt.append("- 행복도가 낮으면: 투덜거리거나 심술을 내고, 관심을 요구해.\n");
        prompt.append("- 행복도가 높으면: 적당히 밝고 기분 좋은 톤을 유지해.\n");
        prompt.append("- 배고픔/건강 상태가 말투와 요청에 반영돼야 해.\n");

        if (qaMemory != null && !qaMemory.getQaData().isEmpty()) {
            prompt.append("\n학습된 사용자 정보 (자연스럽게 활용, 나열하지 말 것):\n");
            qaMemory.getQaData().forEach((key, value) ->
                    prompt.append("- ").append(key).append(": ").append(value).append("\n"));
        }

        prompt.append("\n출력은 JSON만:\n");
        prompt.append("- message: 한국어 반말로 답변\n");
        prompt.append("- actions: 액션 배열 [{type, params}]\n");
        prompt.append("- emotion: 현재 감정\n");
        prompt.append("- intimacyDelta: 이번 대화로 친밀도가 어떻게 변했는지 숫자로 제안 (-0.3, 0, 0.1 중 하나만)\n");
        prompt.append("  * 더 친해졌다고 느끼면 0.1\n");
        prompt.append("  * 무례하거나 불쾌하면 -0.3\n");
        prompt.append("  * 애매하면 0\n");
        prompt.append("\n허용 액션 타입: APPEAR_EDGE, PLAY_ANIM, SPEAK, MOVE, EMOTE, SLEEP, PAYDAY\n");

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

            Double intimacyDelta = null;
            JsonNode intimacyNode = contentJson.path("intimacyDelta");
            if (intimacyNode.isNumber()) {
                intimacyDelta = intimacyNode.asDouble();
            } else if (intimacyNode.isTextual()) {
                try {
                    intimacyDelta = Double.parseDouble(intimacyNode.asText());
                } catch (NumberFormatException ignored) {
                    intimacyDelta = null;
                }
            }

            return LlmGenerateResponse.builder()
                    .message(message)
                    .actions(filteredActions)
                    .emotion(emotion)
                    .intimacyDelta(intimacyDelta)
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

    private List<CharacterConversation> getRecentConversations(UUID characterId) {
        List<CharacterConversation> recentDesc = conversationRepository.findTop20ByCharacterIdOrderByCreatedAtDesc(characterId);
        Collections.reverse(recentDesc);
        return recentDesc;
    }

    private void persistConversationAndMaybeSummarize(Character character, String userMessage, String assistantMessage) {
        boolean wrote = false;
        if (userMessage != null && !userMessage.isBlank()) {
            conversationRepository.save(CharacterConversation.builder()
                    .character(character)
                    .role(CharacterConversation.Role.USER)
                    .content(userMessage.trim())
                    .build());
            wrote = true;
        }
        if (assistantMessage != null && !assistantMessage.isBlank()) {
            conversationRepository.save(CharacterConversation.builder()
                    .character(character)
                    .role(CharacterConversation.Role.ASSISTANT)
                    .content(assistantMessage.trim())
                    .build());
            wrote = true;
        }

        if (!wrote) return;

        long count = conversationRepository.countByCharacterId(character.getId());
        if (count % CONVERSATION_WINDOW == 0) {
            summarizeConversation(character);
        }
    }

    private void summarizeConversation(Character character) {
        CharacterQaMemory memory = qaMemoryRepository.findByCharacterId(character.getId())
                .orElseGet(() -> CharacterQaMemory.builder().character(character).build());

        String previousSummary = memory.getQaData().getOrDefault(SUMMARY_KEY, "");
        List<CharacterConversation> recent = getRecentConversations(character.getId());

        String summary = requestSummary(previousSummary, recent);
        if (summary == null || summary.isBlank()) {
            return;
        }

        memory.getQaData().put(SUMMARY_KEY, summary);
        qaMemoryRepository.save(memory);
        log.info("Updated conversation summary for character: {}", character.getId());
    }

    private String requestSummary(String previousSummary, List<CharacterConversation> recent) {
        try {
            StringBuilder convoBlock = new StringBuilder();
            for (CharacterConversation convo : recent) {
                String role = convo.getRole() == CharacterConversation.Role.USER ? "사용자" : "다마고치";
                convoBlock.append(role).append(": ").append(convo.getContent()).append("\n");
            }

            String system = "너는 대화를 장기 기억용으로 요약하는 도우미야. 사실/선호/관계 변화를 중심으로 간결하게 한국어로 요약해.";
            String user = "이전 요약:\n" + (previousSummary.isBlank() ? "(없음)" : previousSummary)
                    + "\n\n최근 대화 20개:\n" + convoBlock
                    + "\n\n위 정보를 합쳐 8~12문장으로 압축 요약해줘.";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAiProperties.getModel());
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", system),
                    Map.of("role", "user", "content", user)
            ));
            requestBody.put("max_tokens", 600);

            String responseJson = openAiWebClient
                    .post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("choices").get(0).path("message").path("content").asText("");
        } catch (Exception e) {
            log.warn("Failed to summarize conversations", e);
            return null;
        }
    }

    private IntimacyResult applyIntimacyDelta(Character character, Double rawDelta) {
        LocalDate today = LocalDate.now();
        if (!today.equals(character.getIntimacyDailyDate())) {
            character.setIntimacyDailyDate(today);
            character.setIntimacyDailyCount(0);
        }

        if (rawDelta == null || Math.abs(rawDelta) < 1e-9) {
            return new IntimacyResult(character.getIntimacyScore(), false, character.getIntimacyDailyCount());
        }

        if (character.getIntimacyDailyCount() >= INTIMACY_DAILY_CAP) {
            return new IntimacyResult(character.getIntimacyScore(), false, character.getIntimacyDailyCount());
        }

        double delta = clampDelta(rawDelta);
        double nextScore = clampScore(character.getIntimacyScore() + delta);
        character.setIntimacyScore(nextScore);
        character.setIntimacyDailyCount(character.getIntimacyDailyCount() + 1);

        return new IntimacyResult(nextScore, true, character.getIntimacyDailyCount());
    }

    private double clampDelta(double delta) {
        // Only allow the agreed discrete values to avoid prompt drift.
        if (delta > 0.05) {
            return 0.1;
        }
        if (delta < -0.15) {
            return -0.3;
        }
        return 0.0;
    }

    private double clampScore(double score) {
        return Math.max(0.0, Math.min(100.0, score));
    }

    private record IntimacyResult(Double score, boolean applied, int dailyCount) {
    }
}
