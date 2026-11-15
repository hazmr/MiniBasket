package valleysoft.minibasket.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import valleysoft.minibasket.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
