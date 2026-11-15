package valleysoft.minibasket.order.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import valleysoft.minibasket.order.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
