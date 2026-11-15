package valleysoft.minibasket.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import valleysoft.minibasket.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("""
            select u from User u
            join fetch u.role r
            left join fetch r.permissions
            where u.email = :email
            """)
    Optional<User> findWithRoleByEmail(String email);

    @Query("select u from User u join fetch u.role r left join fetch r.permissions where u.id = :id")
    Optional<User> findWithRoleById(Long id);
}
