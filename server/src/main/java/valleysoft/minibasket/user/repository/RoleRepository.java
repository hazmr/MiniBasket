package valleysoft.minibasket.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import valleysoft.minibasket.user.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
