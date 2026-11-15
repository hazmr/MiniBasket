package valleysoft.minibasket.product.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import valleysoft.minibasket.brand.entity.Brand;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.product.dto.ProductRequest;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal discountPrice;

    @Column(nullable = false)
    private int stockQuantity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> attributes = new HashMap<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @BatchSize(size = 100)
    private List<ProductImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static Product create(ProductRequest request, Category category, Brand brand) {
        Product product = new Product();
        product.update(request, category, brand);
        return product;
    }

    public void update(ProductRequest request, Category category, Brand brand) {
        this.sku = request.sku().trim();
        this.name = request.name().trim();
        this.price = request.price();
        this.discountPrice = request.discountPrice();
        this.stockQuantity = request.stockQuantity();
        this.attributes = request.attributes() == null ? new HashMap<>() : new HashMap<>(request.attributes());
        this.category = category;
        this.brand = brand;
    }

    public BigDecimal getEffectivePrice() {
        return discountPrice != null ? discountPrice : price;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public ProductImage getCoverImage() {
        return images.isEmpty() ? null : images.getFirst();
    }

    public ProductImage addImage(String storageKey) {
        ProductImage image = new ProductImage(this, storageKey);
        images.add(image);
        return image;
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
    }
}
