package valleysoft.minibasket.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import valleysoft.minibasket.user.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUserIdOrderByIdAsc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);
}
