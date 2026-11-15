package valleysoft.minibasket.wishlist.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.user.entity.User;

@Entity
@Table(name = "wishlist_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public WishlistItem(User user, Product product) {
        this.user = user;
        this.product = product;
    }
}
