package valleysoft.minibasket.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import valleysoft.minibasket.order.entity.ShippingMethod;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {
}
