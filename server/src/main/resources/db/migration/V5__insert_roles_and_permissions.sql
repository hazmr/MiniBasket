-- Roles are reference data the application needs to run, so they live in a migration.
-- Demo products and users are created by DevDataSeeder in the "dev" profile only.
INSERT INTO roles (name) VALUES ('CUSTOMER'), ('ADMIN');

INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission
FROM roles r
CROSS JOIN (VALUES ('CATALOG_MANAGE'), ('ORDER_MANAGE'), ('COUPON_MANAGE'), ('SHIPPING_MANAGE')) AS p (permission)
WHERE r.name = 'ADMIN';
