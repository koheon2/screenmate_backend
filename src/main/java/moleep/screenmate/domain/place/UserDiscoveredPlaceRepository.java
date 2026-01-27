package moleep.screenmate.domain.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDiscoveredPlaceRepository extends JpaRepository<UserDiscoveredPlace, UUID> {

    @Query("SELECT udp FROM UserDiscoveredPlace udp JOIN FETCH udp.place p WHERE udp.user.id = :userId ORDER BY udp.discoveredAt DESC")
    List<UserDiscoveredPlace> findByUserIdWithDefinition(@Param("userId") UUID userId);

    @Query("SELECT udp FROM UserDiscoveredPlace udp WHERE udp.user.id = :userId AND udp.place.id = :placeId")
    Optional<UserDiscoveredPlace> findByUserIdAndPlaceId(@Param("userId") UUID userId, @Param("placeId") String placeId);
}
