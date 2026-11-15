-- Optimistic locking: every change to a product (an admin edit or stock taken by a checkout)
-- increases version, so a save based on an older copy is refused instead of overwriting it.
ALTER TABLE products ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
