package valleysoft.minibasket.seed;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.brand.entity.Brand;
import valleysoft.minibasket.brand.repository.BrandRepository;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.category.repository.CategoryRepository;
import valleysoft.minibasket.order.entity.Coupon;
import valleysoft.minibasket.order.entity.DiscountType;
import valleysoft.minibasket.order.entity.ShippingMethod;
import valleysoft.minibasket.order.repository.CouponRepository;
import valleysoft.minibasket.order.repository.ShippingMethodRepository;
import valleysoft.minibasket.product.dto.ProductRequest;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.storage.StorageService;
import valleysoft.minibasket.user.entity.Role;
import valleysoft.minibasket.user.entity.User;
import valleysoft.minibasket.user.repository.RoleRepository;
import valleysoft.minibasket.user.repository.UserRepository;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    static final String ADMIN_EMAIL = "admin@minibasket.dev";
    static final String CUSTOMER_EMAIL = "customer@minibasket.dev";

    private static final List<String> DEMO_SKUS = List.of(
            "PHN-NOVA-X1", "PHN-ORBIT-5", "LPT-NOVA-BOOK", "LPT-ORBIT-AIR", "KIT-HOMELY-KETTLE", "KIT-HOMELY-TOASTER");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            createDemoData();
        }
        uploadMissingDemoImages();
    }

    private void createDemoData() {
        createUser(ADMIN_EMAIL, "Admin123!", Role.ADMIN);
        createUser(CUSTOMER_EMAIL, "Customer123!", Role.CUSTOMER);

        Category electronics = categoryRepository.save(new Category("Electronics", null));
        Category phones = categoryRepository.save(new Category("Phones", electronics));
        Category laptops = categoryRepository.save(new Category("Laptops", electronics));
        Category kitchen = categoryRepository.save(new Category("Kitchen", null));

        Brand nova = brandRepository.save(new Brand("Nova"));
        Brand orbit = brandRepository.save(new Brand("Orbit"));
        Brand homely = brandRepository.save(new Brand("Homely"));

        createProduct("PHN-NOVA-X1", "Nova X1 Phone", "699.00", "649.00", 25, phones, nova,
                Map.of("color", "black", "storage", "128GB"));
        createProduct("PHN-ORBIT-5", "Orbit 5 Phone", "499.00", null, 40, phones, orbit,
                Map.of("color", "blue", "storage", "256GB"));
        createProduct("LPT-NOVA-BOOK", "Nova Book 14", "1199.00", null, 10, laptops, nova,
                Map.of("ram", "16GB", "screen", "14 inch"));
        createProduct("LPT-ORBIT-AIR", "Orbit Air 13", "899.00", "849.00", 3, laptops, orbit,
                Map.of("ram", "8GB", "screen", "13 inch"));
        createProduct("KIT-HOMELY-KETTLE", "Homely Electric Kettle", "39.90", null, 100, kitchen, homely,
                Map.of("capacity", "1.7L"));
        createProduct("KIT-HOMELY-TOASTER", "Homely Toaster", "29.90", "24.90", 0, kitchen, homely,
                Map.of("slots", 2));

        shippingMethodRepository.save(new ShippingMethod("Standard (3-5 days)", new BigDecimal("4.99")));
        shippingMethodRepository.save(new ShippingMethod("Express (next day)", new BigDecimal("12.99")));

        couponRepository.save(new Coupon("WELCOME10", DiscountType.PERCENTAGE, new BigDecimal("10"), null));
        couponRepository.save(new Coupon("FIVEOFF", DiscountType.FIXED, new BigDecimal("5.00"), null));

        log.info("Demo data created. Admin: {} / Admin123!  Customer: {} / Customer123!", ADMIN_EMAIL, CUSTOMER_EMAIL);
    }

    private void createUser(String email, String password, String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User(email, passwordEncoder.encode(password), role);
        user.verifyEmail();
        userRepository.save(user);
    }

    private void createProduct(String sku, String name, String price, String discountPrice, int stock,
            Category category, Brand brand, Map<String, Object> attributes) {
        var request = new ProductRequest(sku, name, new BigDecimal(price),
                discountPrice == null ? null : new BigDecimal(discountPrice), stock, category.getId(), brand.getId(),
                attributes, null);
        productRepository.save(Product.create(request, category, brand));
    }

    private void uploadMissingDemoImages() {
        try {
            for (String sku : DEMO_SKUS) {
                productRepository.findBySku(sku).ifPresent(this::uploadDemoImage);
            }
        } catch (RuntimeException ex) {
            log.warn("Could not upload demo images to storage, check the STORAGE_* settings in server/.env: {}", ex.getMessage());
        }
    }

    private void uploadDemoImage(Product product) {
        String key = "products/" + product.getId() + "/demo-" + product.getSku().toLowerCase() + ".svg";
        boolean attached = product.getImages().stream().anyMatch(image -> key.equals(image.getStorageKey()));
        if (attached && storageService.find(key).isPresent()) {
            return;
        }
        storageService.upload(key, readSeedImage(product.getSku()), "image/svg+xml");
        if (!attached) {
            product.addImage(key);
        }
    }

    private static byte[] readSeedImage(String sku) {
        try {
            return new ClassPathResource("seed-images/" + sku + ".svg").getContentAsByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Missing seed image for " + sku, ex);
        }
    }
}
