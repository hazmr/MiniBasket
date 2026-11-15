-- Every product image is now a file in our bucket, so the column for links to other websites goes.
ALTER TABLE product_images DROP CONSTRAINT ck_product_images_one_source;
ALTER TABLE product_images DROP COLUMN image_url;
ALTER TABLE product_images ALTER COLUMN storage_key SET NOT NULL;
