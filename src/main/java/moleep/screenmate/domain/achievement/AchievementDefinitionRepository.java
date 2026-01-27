package moleep.screenmate.domain.achievement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementDefinitionRepository extends JpaRepository<AchievementDefinition, String> {
}
