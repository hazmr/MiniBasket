CREATE TABLE categories (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT REFERENCES categories (id),
    name      VARCHAR(100) NOT NULL
);

CREATE INDEX idx_categories_parent_id ON categories (parent_id);

CREATE TABLE brands (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id    BIGINT         NOT NULL REFERENCES categories (id),
    brand_id       BIGINT REFERENCES brands (id),
    sku            VARCHAR(64)    NOT NULL UNIQUE,
    name           VARCHAR(200)   NOT NULL,
    price          NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    discount_price NUMERIC(12, 2) CHECK (discount_price >= 0),
    stock_quantity INTEGER        NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    attributes     JSONB          NOT NULL DEFAULT '{}',
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_brand_id ON products (brand_id);

CREATE TABLE product_images (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT       NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    image_url  VARCHAR(500) NOT NULL
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);
