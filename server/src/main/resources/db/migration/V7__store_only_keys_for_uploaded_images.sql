-- Uploaded images keep only their storage key; the API builds the public URL from
-- app.storage.public-url, so moving to another bucket or domain needs no data change.
-- image_url now only holds links to images hosted on other websites.
ALTER TABLE product_images ALTER COLUMN image_url DROP NOT NULL;

UPDATE product_images SET image_url = NULL WHERE storage_key IS NOT NULL;

ALTER TABLE product_images ADD CONSTRAINT ck_product_images_one_source
    CHECK ((image_url IS NULL) <> (storage_key IS NULL));
