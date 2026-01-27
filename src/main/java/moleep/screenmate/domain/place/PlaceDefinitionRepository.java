package moleep.screenmate.domain.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceDefinitionRepository extends JpaRepository<PlaceDefinition, String> {
}
