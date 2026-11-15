package valleysoft.minibasket.cart.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import valleysoft.minibasket.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("select c from CartItem c join fetch c.product where c.user.id = :userId order by c.id")
    List<CartItem> findAllWithProductByUserId(Long userId);

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Query("delete from CartItem c where c.user.id = :userId")
    void deleteAllByUserId(Long userId);
}
