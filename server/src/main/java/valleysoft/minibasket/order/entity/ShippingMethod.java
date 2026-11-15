package valleysoft.minibasket.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipping_methods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal fee;

    public ShippingMethod(String name, BigDecimal fee) {
        this.name = name;
        this.fee = fee;
    }

    public void update(String name, BigDecimal fee) {
        this.name = name;
        this.fee = fee;
    }
}
