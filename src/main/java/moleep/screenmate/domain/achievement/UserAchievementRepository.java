package moleep.screenmate.domain.achievement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    @Query("SELECT ua FROM UserAchievement ua JOIN FETCH ua.achievement a WHERE ua.user.id = :userId ORDER BY ua.unlockedAt DESC NULLS LAST, ua.createdAt DESC")
    List<UserAchievement> findByUserIdWithDefinition(@Param("userId") UUID userId);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId AND ua.achievement.id = :achievementId")
    Optional<UserAchievement> findByUserIdAndAchievementId(@Param("userId") UUID userId, @Param("achievementId") String achievementId);
}
