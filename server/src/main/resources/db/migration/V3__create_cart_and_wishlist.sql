CREATE TABLE cart_items (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id BIGINT      NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity   INTEGER     NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- one row per product in a cart; adding the same product again raises the quantity
    CONSTRAINT uq_cart_items_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);

CREATE TABLE wishlist_items (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id BIGINT      NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wishlist_items_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_wishlist_items_product_id ON wishlist_items (product_id);
