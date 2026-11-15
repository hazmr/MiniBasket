package valleysoft.minibasket.product.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import valleysoft.minibasket.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("""
            select p from Product p
            where (:categoryId is null or p.category.id = :categoryId)
              and (:brandId is null or p.brand.id = :brandId)
              and lower(p.name) like lower(concat('%', :name, '%'))
            """)
    Page<Product> search(Long categoryId, Long brandId, String name, Pageable pageable);

    @Query("select p.stockQuantity from Product p where p.id = :productId")
    int stockOf(Long productId);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Product p set p.stockQuantity = p.stockQuantity - :quantity, p.version = p.version + 1
            where p.id = :productId and p.stockQuantity >= :quantity
            """)
    int decreaseStock(Long productId, int quantity);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Product p set p.stockQuantity = p.stockQuantity + :quantity, p.version = p.version + 1
            where p.id = :productId
            """)
    int increaseStock(Long productId, int quantity);
}
