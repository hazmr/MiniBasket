CREATE TABLE shipping_methods (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100)   NOT NULL,
    fee  NUMERIC(12, 2) NOT NULL CHECK (fee >= 0)
);

CREATE TABLE coupons (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code           VARCHAR(50)    NOT NULL UNIQUE,
    discount_type  VARCHAR(20)    NOT NULL,
    discount_value NUMERIC(12, 2) NOT NULL CHECK (discount_value > 0),
    -- NULL means the coupon never expires
    expires_at     TIMESTAMPTZ
);

CREATE TABLE orders (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_number       VARCHAR(30)    NOT NULL UNIQUE,
    user_id            BIGINT         NOT NULL REFERENCES users (id),
    coupon_id          BIGINT REFERENCES coupons (id),
    shipping_method_id BIGINT         NOT NULL REFERENCES shipping_methods (id),
    status             VARCHAR(20)    NOT NULL,
    payment_status     VARCHAR(20)    NOT NULL,
    subtotal           NUMERIC(12, 2) NOT NULL,
    discount_amount    NUMERIC(12, 2) NOT NULL,
    shipping_fee       NUMERIC(12, 2) NOT NULL,
    total              NUMERIC(12, 2) NOT NULL,
    tracking_number    VARCHAR(100),
    -- copied from the address at checkout so later address edits don't change past orders
    shipping_street    VARCHAR(255)   NOT NULL,
    shipping_city      VARCHAR(100)   NOT NULL,
    version            BIGINT         NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_coupon_id ON orders (coupon_id);
CREATE INDEX idx_orders_shipping_method_id ON orders (shipping_method_id);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id BIGINT         NOT NULL REFERENCES products (id),
    -- the price at the moment of purchase, not the current product price
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity   INTEGER        NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
