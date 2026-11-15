package valleysoft.minibasket.order.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import valleysoft.minibasket.order.entity.Order;
import valleysoft.minibasket.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    @Query("""
            select distinct o from Order o
            left join fetch o.items i
            left join fetch i.product
            where o.id = :id
            """)
    Optional<Order> findWithItemsById(Long id);
}
