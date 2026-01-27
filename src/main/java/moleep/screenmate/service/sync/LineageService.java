package moleep.screenmate.service.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moleep.screenmate.domain.character.Character;
import moleep.screenmate.domain.character.CharacterRepository;
import moleep.screenmate.domain.lineage.CharacterLineage;
import moleep.screenmate.domain.lineage.CharacterLineageRepository;
import moleep.screenmate.domain.user.User;
import moleep.screenmate.dto.lineage.LineageCreateRequest;
import moleep.screenmate.dto.lineage.LineageEdgeResponse;
import moleep.screenmate.dto.lineage.LineageGraphResponse;
import moleep.screenmate.dto.lineage.LineageNodeResponse;
import moleep.screenmate.exception.BadRequestException;
import moleep.screenmate.exception.ConflictException;
import moleep.screenmate.exception.NotFoundException;
import moleep.screenmate.validation.OwnershipValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineageService {

    private static final int DEFAULT_DEPTH = 3;
    private static final int MAX_DEPTH = 6;

    private final OwnershipValidator ownershipValidator;
    private final CharacterRepository characterRepository;
    private final CharacterLineageRepository lineageRepository;

    @Transactional
    public LineageEdgeResponse createLineage(UUID childCharacterId, User user, LineageCreateRequest request) {
        Character child = ownershipValidator.validateAndGetCharacter(childCharacterId, user);

        if (lineageRepository.existsByChildCharacterId(childCharacterId)) {
            throw new ConflictException("LINEAGE_ALREADY_EXISTS", "Lineage already exists for this child");
        }

        Character parentA = getCharacterOrThrow(request.getParentACharacterId());
        Character parentB = getCharacterOrThrow(request.getParentBCharacterId());

        validateLineage(child, parentA, parentB);

        CharacterLineage lineage = CharacterLineage.builder()
                .childCharacter(child)
                .parentACharacter(parentA)
                .parentBCharacter(parentB)
                .build();

        CharacterLineage saved = lineageRepository.save(lineage);
        log.info("Lineage created: child={}, parentA={}, parentB={}", child.getId(), parentA.getId(), parentB.getId());
        return LineageEdgeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LineageGraphResponse getLineageGraph(UUID rootCharacterId, User user, Integer depth) {
        ownershipValidator.validateOwnership(rootCharacterId, user);

        int resolvedDepth = depth == null ? DEFAULT_DEPTH : Math.min(Math.max(depth, 1), MAX_DEPTH);
        Set<UUID> visitedIds = new HashSet<>();
        visitedIds.add(rootCharacterId);

        Set<UUID> frontier = new HashSet<>();
        frontier.add(rootCharacterId);

        Map<UUID, CharacterLineage> edgeMap = new HashMap<>();

        for (int i = 0; i < resolvedDepth && !frontier.isEmpty(); i++) {
            List<CharacterLineage> connected = lineageRepository.findConnectedEdges(frontier);
            if (connected.isEmpty()) {
                break;
            }

            Set<UUID> nextFrontier = new HashSet<>();
            for (CharacterLineage edge : connected) {
                edgeMap.put(edge.getId(), edge);
                for (UUID id : extractIds(edge)) {
                    if (visitedIds.add(id)) {
                        nextFrontier.add(id);
                    }
                }
            }

            frontier = nextFrontier;
        }

        List<Character> characters = fetchCharactersWithUser(visitedIds);
        List<LineageNodeResponse> nodes = characters.stream()
                .sorted(Comparator.comparing(Character::getCreatedAt))
                .map(LineageNodeResponse::from)
                .toList();

        List<LineageEdgeResponse> edges = edgeMap.values().stream()
                .sorted(Comparator.comparing(CharacterLineage::getCreatedAt))
                .map(LineageEdgeResponse::from)
                .toList();

        return LineageGraphResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    private Character getCharacterOrThrow(UUID characterId) {
        return characterRepository.findById(characterId)
                .orElseThrow(() -> new NotFoundException("CHARACTER_NOT_FOUND", "Character not found"));
    }

    private void validateLineage(Character child, Character parentA, Character parentB) {
        if (child.getId().equals(parentA.getId()) || child.getId().equals(parentB.getId())) {
            throw new BadRequestException("INVALID_LINEAGE", "Child cannot be the same as a parent");
        }
        if (parentA.getId().equals(parentB.getId())) {
            throw new BadRequestException("INVALID_LINEAGE", "Parents must be different characters");
        }
    }

    private Collection<UUID> extractIds(CharacterLineage edge) {
        List<UUID> ids = new ArrayList<>(3);
        ids.add(edge.getChildCharacter().getId());
        ids.add(edge.getParentACharacter().getId());
        ids.add(edge.getParentBCharacter().getId());
        return ids;
    }

    private List<Character> fetchCharactersWithUser(Set<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return characterRepository.findAllByIdInWithUser(new ArrayList<>(ids));
    }
}

