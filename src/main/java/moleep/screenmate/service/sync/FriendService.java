package moleep.screenmate.service.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.character.CharacterRepository;
import moleep.screenmate.domain.friend.CharacterFriendMessage;
import moleep.screenmate.domain.friend.CharacterFriendMessageRepository;
import moleep.screenmate.domain.friend.CharacterFriendRequest;
import moleep.screenmate.domain.friend.CharacterFriendRequestRepository;
import moleep.screenmate.domain.friend.CharacterFriendship;
import moleep.screenmate.domain.friend.CharacterFriendshipRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.friend.CharacterSearchResponse;
import moleep.screenmate.dto.friend.FriendMessageCreateRequest;
import moleep.screenmate.dto.friend.FriendMessageResponse;
import moleep.screenmate.dto.friend.FriendRequestCreateRequest;
import moleep.screenmate.dto.friend.FriendRequestResponse;
import moleep.screenmate.dto.friend.FriendshipResponse;
import moleep.screenmate.exception.BadRequestException;
import moleep.screenmate.exception.ConflictException;
import moleep.screenmate.exception.NotFoundException;
import moleep.screenmate.validation.OwnershipValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    private static final int MAX_INTIMACY = 100;
    private static final int DEFAULT_MESSAGE_LIMIT = 20;

    private final OwnershipValidator ownershipValidator;
    private final CharacterRepository characterRepository;
    private final CharacterFriendRequestRepository friendRequestRepository;
    private final CharacterFriendshipRepository friendshipRepository;
    private final CharacterFriendMessageRepository messageRepository;

    @Transactional
    public FriendRequestResponse sendFriendRequest(UUID requesterCharacterId, User user, FriendRequestCreateRequest request) {
        Character requester = ownershipValidator.validateAndGetCharacter(requesterCharacterId, user);
        Character receiver = characterRepository.findById(request.getTargetCharacterId())
                .orElseThrow(() -> new NotFoundException("CHARACTER_NOT_FOUND", "Target character not found"));

        if (requester.getId().equals(receiver.getId())) {
            throw new BadRequestException("INVALID_FRIEND_REQUEST", "Cannot send friend request to self");
        }

        if (receiver.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("INVALID_FRIEND_REQUEST", "Cannot send friend request to your own character");
        }

        ensureNotAlreadyFriends(requester.getId(), receiver.getId());
        ensureNoPendingRequest(requester.getId(), receiver.getId());

        CharacterFriendRequest friendRequest = CharacterFriendRequest.builder()
                .requesterCharacter(requester)
                .receiverCharacter(receiver)
                .message(request.getMessage())
                .status(CharacterFriendRequest.Status.PENDING)
                .build();

        friendRequest = friendRequestRepository.save(friendRequest);
        log.info("Friend request created: {} -> {}", requester.getId(), receiver.getId());

        return FriendRequestResponse.from(friendRequest);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getFriendRequests(UUID characterId, User user, String direction, String status) {
        ownershipValidator.validateOwnership(characterId, user);

        CharacterFriendRequest.Status resolvedStatus = parseStatus(status);

        List<CharacterFriendRequest> requests;
        if ("outgoing".equalsIgnoreCase(direction)) {
            requests = friendRequestRepository.findOutgoingByCharacterIdAndStatus(characterId, resolvedStatus);
        } else {
            requests = friendRequestRepository.findIncomingByCharacterIdAndStatus(characterId, resolvedStatus);
        }

        return requests.stream()
                .sorted(Comparator.comparing(CharacterFriendRequest::getCreatedAt).reversed())
                .map(FriendRequestResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public FriendRequestResponse acceptFriendRequest(UUID receiverCharacterId, User user, UUID requestId) {
        ownershipValidator.validateOwnership(receiverCharacterId, user);

        CharacterFriendRequest request = friendRequestRepository.findByIdAndReceiver(requestId, receiverCharacterId)
                .orElseThrow(() -> new NotFoundException("FRIEND_REQUEST_NOT_FOUND", "Friend request not found"));

        if (request.getStatus() != CharacterFriendRequest.Status.PENDING) {
            throw new ConflictException("FRIEND_REQUEST_NOT_PENDING", "Friend request is not pending");
        }

        Character requester = request.getRequesterCharacter();
        Character receiver = request.getReceiverCharacter();
        createFriendshipIfAbsent(requester, receiver);

        request.setStatus(CharacterFriendRequest.Status.ACCEPTED);
        request.setRespondedAt(Instant.now());
        friendRequestRepository.save(request);

        log.info("Friend request accepted: {} -> {}", requester.getId(), receiver.getId());

        return FriendRequestResponse.from(request);
    }

    @Transactional
    public FriendRequestResponse rejectFriendRequest(UUID receiverCharacterId, User user, UUID requestId) {
        ownershipValidator.validateOwnership(receiverCharacterId, user);

        CharacterFriendRequest request = friendRequestRepository.findByIdAndReceiver(requestId, receiverCharacterId)
                .orElseThrow(() -> new NotFoundException("FRIEND_REQUEST_NOT_FOUND", "Friend request not found"));

        if (request.getStatus() != CharacterFriendRequest.Status.PENDING) {
            throw new ConflictException("FRIEND_REQUEST_NOT_PENDING", "Friend request is not pending");
        }

        request.setStatus(CharacterFriendRequest.Status.REJECTED);
        request.setRespondedAt(Instant.now());
        friendRequestRepository.save(request);

        log.info("Friend request rejected: {} -> {}", request.getRequesterCharacter().getId(), request.getReceiverCharacter().getId());

        return FriendRequestResponse.from(request);
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> getFriends(UUID characterId, User user) {
        Character self = ownershipValidator.validateAndGetCharacter(characterId, user);
        List<CharacterFriendship> friendships = friendshipRepository.findByCharacterId(characterId);

        return friendships.stream()
                .map(friendship -> mapFriendshipResponse(friendship, self))
                .collect(Collectors.toList());
    }

    @Transactional
    public FriendMessageResponse sendFriendMessage(UUID characterId, UUID friendCharacterId, User user, FriendMessageCreateRequest request) {
        Character sender = ownershipValidator.validateAndGetCharacter(characterId, user);
        CharacterFriendship friendship = getFriendshipOrThrow(characterId, friendCharacterId);

        validateMessageRequest(request);

        CharacterFriendMessage message = CharacterFriendMessage.builder()
                .friendship(friendship)
                .senderCharacter(sender)
                .messageText(normalizeText(request.getMessageText()))
                .emoteId(normalizeText(request.getEmoteId()))
                .build();

        message = messageRepository.save(message);
        incrementIntimacy(friendship);

        log.info("Friend message sent: {} -> {}", characterId, friendCharacterId);

        return FriendMessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public List<FriendMessageResponse> getFriendMessages(UUID characterId, UUID friendCharacterId, User user, Integer limit) {
        ownershipValidator.validateOwnership(characterId, user);
        CharacterFriendship friendship = getFriendshipOrThrow(characterId, friendCharacterId);

        int resolvedLimit = limit == null ? DEFAULT_MESSAGE_LIMIT : Math.min(limit, 100);
        List<CharacterFriendMessage> messages = messageRepository.findRecentByFriendshipId(
                friendship.getId(),
                PageRequest.of(0, resolvedLimit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return messages.stream()
                .sorted(Comparator.comparing(CharacterFriendMessage::getCreatedAt))
                .map(FriendMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CharacterSearchResponse> searchCharacters(User user, String query, Integer limit) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("INVALID_QUERY", "Search query is required");
        }

        int resolvedLimit = limit == null ? 20 : Math.min(limit, 50);
        List<Character> characters = characterRepository.searchByQuery(query, PageRequest.of(0, resolvedLimit));

        return characters.stream()
                .filter(character -> !character.getUser().getId().equals(user.getId()))
                .map(CharacterSearchResponse::from)
                .collect(Collectors.toList());
    }

    private void validateMessageRequest(FriendMessageCreateRequest request) {
        String messageText = normalizeText(request.getMessageText());
        String emoteId = normalizeText(request.getEmoteId());

        if ((messageText == null || messageText.isBlank()) && (emoteId == null || emoteId.isBlank())) {
            throw new BadRequestException("EMPTY_MESSAGE", "Either messageText or emoteId is required");
        }
    }

    private void ensureNotAlreadyFriends(UUID characterId, UUID friendCharacterId) {
        if (getFriendshipOptional(characterId, friendCharacterId).isPresent()) {
            throw new ConflictException("ALREADY_FRIENDS", "Characters are already friends");
        }
    }

    private void ensureNoPendingRequest(UUID characterId, UUID friendCharacterId) {
        boolean outgoingExists = friendRequestRepository
                .findByRequesterAndReceiverAndStatus(characterId, friendCharacterId, CharacterFriendRequest.Status.PENDING)
                .isPresent();
        boolean incomingExists = friendRequestRepository
                .findByRequesterAndReceiverAndStatus(friendCharacterId, characterId, CharacterFriendRequest.Status.PENDING)
                .isPresent();

        if (outgoingExists || incomingExists) {
            throw new ConflictException("FRIEND_REQUEST_EXISTS", "Pending friend request already exists");
        }
    }

    private CharacterFriendship getFriendshipOrThrow(UUID characterId, UUID friendCharacterId) {
        return getFriendshipOptional(characterId, friendCharacterId)
                .orElseThrow(() -> new NotFoundException("FRIENDSHIP_NOT_FOUND", "Friendship not found"));
    }

    private Optional<CharacterFriendship> getFriendshipOptional(UUID characterId, UUID friendCharacterId) {
        UUID a = characterId.compareTo(friendCharacterId) < 0 ? characterId : friendCharacterId;
        UUID b = characterId.compareTo(friendCharacterId) < 0 ? friendCharacterId : characterId;
        return friendshipRepository.findByCharacterPair(a, b);
    }

    private void incrementIntimacy(CharacterFriendship friendship) {
        int next = Math.min(friendship.getIntimacy() + 1, MAX_INTIMACY);
        friendship.setIntimacy(next);
        friendshipRepository.save(friendship);
    }

    private FriendshipResponse mapFriendshipResponse(CharacterFriendship friendship, Character self) {
        Character friend = friendship.getCharacterA().getId().equals(self.getId())
                ? friendship.getCharacterB()
                : friendship.getCharacterA();
        return FriendshipResponse.from(friendship, self, friend);
    }

    private CharacterFriendship createFriendshipIfAbsent(Character requester, Character receiver) {
        UUID aId = requester.getId().compareTo(receiver.getId()) <= 0 ? requester.getId() : receiver.getId();
        UUID bId = requester.getId().compareTo(receiver.getId()) <= 0 ? receiver.getId() : requester.getId();

        return friendshipRepository.findByCharacterPair(aId, bId)
                .orElseGet(() -> {
                    Character a = requester.getId().equals(aId) ? requester : receiver;
                    Character b = requester.getId().equals(aId) ? receiver : requester;
                    CharacterFriendship friendship = CharacterFriendship.builder()
                            .characterA(a)
                            .characterB(b)
                            .intimacy(0)
                            .build();
                    return friendshipRepository.save(friendship);
                });
    }

    private CharacterFriendRequest.Status parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return CharacterFriendRequest.Status.PENDING;
        }
        try {
            return CharacterFriendRequest.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("INVALID_STATUS", "Invalid status value");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
