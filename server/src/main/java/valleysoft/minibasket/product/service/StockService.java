package valleysoft.minibasket.product.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class StockService {

    private final ProductRepository productRepository;

    public void take(Product product, int quantity) {
        if (productRepository.decreaseStock(product.getId(), quantity) == 0) {
            throw new DomainException(ErrorCode.INSUFFICIENT_STOCK,
                    Map.of("available", productRepository.stockOf(product.getId()), "productName", product.getName()));
        }
    }

    public void putBack(Product product, int quantity) {
        productRepository.increaseStock(product.getId(), quantity);
    }
}
