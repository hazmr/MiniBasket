package valleysoft.minibasket.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import valleysoft.minibasket.auth.entity.VerificationToken;
import valleysoft.minibasket.auth.entity.VerificationTokenType;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    @Query("select t from VerificationToken t join fetch t.user where t.token = :token and t.type = :type")
    Optional<VerificationToken> findWithUser(String token, VerificationTokenType type);

    @Modifying
    @Query("delete from VerificationToken t where t.user.id = :userId and t.type = :type")
    void deleteAllForUser(Long userId, VerificationTokenType type);
}
