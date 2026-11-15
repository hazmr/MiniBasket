package valleysoft.minibasket.wishlist.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import valleysoft.minibasket.wishlist.entity.WishlistItem;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @Query("select w from WishlistItem w join fetch w.product where w.user.id = :userId order by w.id desc")
    List<WishlistItem> findAllWithProductByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Query("delete from WishlistItem w where w.user.id = :userId and w.product.id = :productId")
    int deleteByUserIdAndProductId(Long userId, Long productId);
}
