-- Images uploaded to object storage (R2/S3) remember their key, so the file can be deleted with the row.
-- Images that point at another website keep storage_key NULL.
ALTER TABLE product_images ADD COLUMN storage_key VARCHAR(300);

CREATE UNIQUE INDEX uq_product_images_storage_key ON product_images (storage_key);
