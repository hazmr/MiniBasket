package valleysoft.minibasket.brand.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import valleysoft.minibasket.brand.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
