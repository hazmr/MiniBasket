# MiniBasket server

The Spring Boot API behind MiniBasket, a small online shop: customers browse products, fill a cart, check out with a coupon and
pay; admins manage the catalog, coupons, shipping methods and order status.

**Stack:** Java 25 · Spring Boot 4.1 · Spring Security (JWT) · Spring Data JPA · Flyway · PostgreSQL 18 · Redis · MapStruct · Lombok

## Run it

You need Java 25 and Docker. Nothing else; Maven comes with the project (`./mvnw`).

```bash
cd server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

In IntelliJ, pick the **MiniBasketApplication (dev)** run configuration (it lives in `.run/`).

That one command starts four containers from `compose.yaml` (Postgres, Redis for the cache and
login limits, MinIO for images and Mailpit for emails), creates the tables (Flyway), fills in demo
data, and starts the API on <http://localhost:8080>.

Open **Swagger UI** at <http://localhost:8080/swagger-ui.html> to try every endpoint.
Log in with `POST /api/v1/auth/login`, copy the `accessToken`, click **Authorize** and paste it.

| Demo account | Password |
|---|---|
| `admin@minibasket.dev` | `Admin123!` |
| `customer@minibasket.dev` | `Customer123!` |

Demo coupons: `WELCOME10` (10% off) and `FIVEOFF` (5.00 off).

Outside the `dev` profile the app needs a `JWT_SECRET` environment variable
(generate one with `openssl rand -base64 32`).

## Images and email

Both work out of the box locally, and switch to the real services by filling in `server/.env`
(copy `.env.example`; git ignores `.env`). The code is the same either way.

| | Local (default in `dev`) | Real |
|---|---|---|
| Product images | MinIO on <http://localhost:9000>, bucket `minibasket` | Cloudflare R2 (or AWS S3) |
| Emails | Mailpit: read them at <http://localhost:8026> | Gmail SMTP with an app password |

**How an image upload works:** the admin page asks the API for a signed upload link (valid 10 minutes,
only JPEG/PNG/WebP up to 5 MB), the browser uploads the file **straight to the bucket**, then the API
checks the file arrived and attaches it to the product. Files are deleted from the bucket only after
the database change commits. On startup, the dev seeder uploads the six demo pictures from
`src/main/resources/seed-images/` into whichever bucket is configured.

**Cloudflare R2 setup**
1. Create the bucket, then Settings → Public access → enable the **R2.dev subdomain**.
2. Settings → **CORS policy**, so browsers may upload to it:
   ```json
   [{ "AllowedOrigins": ["http://localhost:3000"], "AllowedMethods": ["PUT"], "AllowedHeaders": ["content-type"], "MaxAgeSeconds": 3600 }]
   ```
3. R2 → Manage API tokens → create a token with **Object Read & Write** on that bucket.
4. Put the endpoint, keys, bucket and public URL in `server/.env` (see `.env.example`).

**Gmail setup:** turn on 2-Step Verification, create an **app password** (Google account → Security →
App passwords), and put it in `server/.env` with the `MAIL_*` lines from `.env.example`. Emails are
sent after the database commits, on a background thread, so sign-up never waits for Gmail.
The emails are Thymeleaf templates in `src/main/resources/templates/mail/`.

`app.email.enabled` (env `MAIL_ENABLED`) picks the mail bean with `@ConditionalOnProperty`:
`SmtpEmailSender` sends real emails, `LoggingEmailSender` only logs them. It defaults to `true`
in the dev profile (Mailpit) and `false` everywhere else until you configure SMTP.

Uploaded images store only their **key** in the database; the API builds the public URL from
`app.storage.public-url`, so moving from MinIO to R2 (or to a custom domain) needs no data change.

## Redis: cache and rate limits

Redis does two jobs. Nothing in it is precious: if it is wiped, the app rebuilds what it needs.

**1. Cache for lists every page reads but only admins change:** categories, brands and shipping
methods. `@Cacheable` on the service's list method stores the result for 10 minutes. `@CacheEvict`
on create/update/delete removes it **after the database commit**, so the next read loads the new list.

```java
@Cacheable(cacheNames = CacheNames.BRANDS, key = CacheNames.ALL)
public List<BrandResponse> listBrands() { ... }

@CacheEvict(cacheNames = CacheNames.BRANDS, key = CacheNames.ALL)
public BrandResponse createBrand(BrandRequest request) { ... }
```

- **Products are not cached**, on purpose: their stock changes with every order, so a cached
  product would show stock that is already sold.
- Each cache is declared in `CacheConfig` with its exact type, so Redis holds plain JSON
  (`[{"id":1,"name":"Phones"}]`) and nothing in Redis can make Jackson create a class we didn't
  choose. `RedisIT` reads a value back to prove it comes out as the same record type.
- To add a cache: a name in `CacheNames`, its type in `CacheConfig`, and the two annotations.

**2. Rate limits** (`security/RateLimiter`): at most 10 logins per email every 15 minutes, right or wrong
password, and 3 password-reset emails per address per hour. After that the API answers
`429 TOO_MANY_ATTEMPTS` with `params.minutes`. The counter is one atomic Lua script, and the key holds a
hash of the email, not the address. The limits are in `app.rate-limit.*`.

Limits are per email, not per IP: every request reaches the API from the Next.js server, so an IP
limit would throttle all customers together.

**When Redis is down**, the two jobs behave differently, on purpose:

| | Redis down | Why |
|---|---|---|
| Cache | Reads go to the database (a bit slower) | A cache must never break the shop |
| Rate limit | Login and reset answer `503 SERVICE_UNAVAILABLE` | Otherwise an attacker gets unlimited guesses |

Commands time out after 2 seconds (`spring.data.redis.timeout`) instead of Lettuce's default 60.
`/actuator/health` shows Redis, but the readiness probe doesn't depend on it, so a load balancer
doesn't take every server offline because the cache is down. In production, point the app at a
managed Redis with `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` and `REDIS_SSL` (see `.env.example`).

## Tests

```bash
./mvnw test      # unit tests, no Docker needed (a few seconds)
./mvnw verify    # + end-to-end tests against real Postgres, Redis, MinIO and SMTP (needs Docker)
```

- **Unit tests** (`*Test`) check the business rules in the services: login rules, cart stock
  checks, order totals with coupons, out-of-stock checkout, cancelling and restocking, image rules,
  the error codes, and the product mapper.
- **End-to-end tests** (`*IT`) start the whole app against real services from `TestInfrastructure`:
  Postgres, Redis and MinIO in containers (Testcontainers) and an in-memory SMTP server (GreenMail).
  - `CheckoutFlowIT`: register → verify → login → cart → checkout → pay.
  - `SecurityIT`: who may call what (401/403/404) and the error format.
  - `EmailIT`: the real verification and reset emails arrive, and their links work.
  - `ProductImageIT`: presigned upload to the bucket, public read, deletion after removal.
  - `ProductConcurrencyIT`: an admin saving an old product form can't undo stock sold meanwhile.
  - `RedisIT`: the cache fills, expires and is cleared by admin changes; login and reset-link limits.
  - `ErrorContractIT`: error codes, params and trace IDs, Actuator access, and the error list in the API docs.

## How the code is organised

The code has two kinds of packages, and each kind follows one rule everywhere.

> All code lives in the package `valleysoft.minibasket` (company, then project), so the folders
> under `src/main/java` are `valleysoft/minibasket`. We left out the usual `org.` prefix
> (`valleysoft.org` reversed) to keep paths short. The Maven groupId is still `org.valleysoft`.

**Features** are the shop's business areas. Each one has the same layers, and a layer folder exists
only when the feature needs it (`auth` has no entity of its own to map, so it has no `mapper/`):

```
valleysoft.minibasket
├── auth/        register, verify email, login, password reset
├── user/        current user, addresses, roles and permissions
├── category/    product categories (nested)
├── brand/
├── product/     products, their images and their stock
├── cart/
├── wishlist/
└── order/       checkout, payment, order status, coupons, shipping methods
      ├── controller/   HTTP only: read the request, call one service method, return a DTO
      ├── service/      business rules and @Transactional
      ├── repository/   database access (Spring Data interfaces)
      ├── entity/       JPA entities and enums
      ├── dto/          request and response records (what the API sends and receives)
      ├── mapper/       MapStruct interfaces that turn entities into response records
      └── event/        events the feature publishes (only auth has some)
```

**Infrastructure** is technical code every feature can use. One flat package per concern, with
its settings record (`…Properties`) next to the code that reads it:

```
├── common/      shared by all features: errors (exception/), PageResponse (dto/), cache names,
│                the MapStruct config and the trace-id filter
├── config/      only @Configuration classes: security rules, cache, async, Swagger, storage client
├── security/    JWT, the request filter, the logged-in user, rate limiting
├── storage/     the S3/R2 bucket: signed uploads, file checks, deleting unused files
├── email/       sending an email (SMTP or log); what to send is decided by the feature
└── seed/        demo data for the dev profile
```

Features use infrastructure; infrastructure doesn't use features, with three deliberate exceptions:
`config/` wires things together (`CacheConfig` names the type of each cache), `security/` has to
load the logged-in `User`, and `seed/` fills every feature with demo data.

**One class, one reason to change.** Each controller serves one resource (`AdminBrandController`
is only `/admin/brands`), and each service owns one set of rules: `ProductService` for product
data, `ProductImageService` for the upload workflow, `StockService` for stock, `CouponService`
for coupon rules. A service may *read* another feature's data through its repository, but
*changes* go through that feature's service: checkout takes stock with `StockService` and empties
the cart with `CartService.clearCart`, so each rule lives in exactly one place.

The database schema lives in `src/main/resources/db/migration` (Flyway). Hibernate never changes
the schema; it only checks on startup that the entities match it (`ddl-auto: validate`).

### One request, start to finish: `POST /api/v1/orders`

1. **`JwtAuthenticationFilter`** reads `Authorization: Bearer …`, checks the token, loads the user
   and marks the request as logged in. `SecurityConfig` decides the URL needs a logged-in user.
2. **`OrderController.placeOrder`** validates the JSON body (`@Valid PlaceOrderRequest`) and passes
   the user's id to the service. That's all a controller does.
3. **`OrderService.placeOrder`** runs in one transaction and coordinates the checkout: it loads the
   cart, the address and the shipping method, asks `CouponService` for a valid coupon, asks
   `StockService` to take stock for each item, gets a number from `OrderNumberGenerator`, builds
   the `Order`, saves it and empties the cart. If any step fails, everything is rolled back.
   Each helper owns one rule, so `OrderService` reads as the list of checkout steps.
4. **`Order`** (the entity) owns the order rules: totals, and which status changes are allowed
   (you can't ship an unpaid order, or cancel a shipped one).
5. The service returns an **`OrderResponse`** record, built by `OrderMapper`. Entities never leave the service layer.
6. If something goes wrong, **`GlobalExceptionHandler`** turns the exception into an error response.

### Errors

Every error uses the standard Problem Details format (RFC 9457), plus three fields clients rely on:

```json
{
  "status": 422,
  "title": "Unprocessable Content",
  "detail": "Only 2 of “Nova X1 Phone” left in stock",
  "instance": "/api/v1/cart/items",
  "errorCode": "INSUFFICIENT_STOCK",
  "params": { "available": 2, "productName": "Nova X1 Phone" },
  "traceId": "3f1c9a52-0d7e-4c55-9a40-1b2f8e6d7c10"
}
```

- **`errorCode`** says *what* went wrong. Every code lives in one enum, `common/exception/ErrorCode`,
  with its HTTP status and an English template. Clients pick their own wording by this code, so a
  released code is never renamed or reused.
- **`params`** are the values for the template's `{placeholders}`, so a client can write
  "Only 2 left" in any language without parsing `detail`.
- **`traceId`** is the same as the `X-Request-Id` response header and appears in every log line
  of that request, in square brackets. When a user reports "reference 3f1c…",
  search the logs for it.

Validation errors (`VALIDATION_FAILED`) also list `violations`: `{field, code, params, message}`,
for example `{"field": "password", "code": "Size", "params": {"min": 8, "max": 72}}`.

Services throw one exception type:

```java
throw new DomainException(ErrorCode.INSUFFICIENT_STOCK, Map.of("available", 2, "productName", name));
```

`DomainException` checks the map has exactly the template's placeholders, so a forgotten or
misspelled param fails the tests instead of reaching a client.

**Adding an error:** add a constant to `ErrorCode`, throw it, then in `client/` run
`npm run generate:api` and add its message to `src/lib/error-messages.ts` (the build fails until you do).
The full list is in Swagger under the `ErrorCode` schema.

### Monitoring

Actuator runs next to the API:

- `GET /actuator/health` (public; `/health/liveness` and `/health/readiness` for Kubernetes probes)
- `GET /actuator/metrics` and `/actuator/info` (admin token only)
- `minibasket.api.errors` counts every error response by `code` and `status`, e.g.
  `/actuator/metrics/minibasket.api.errors?tag=code:INSUFFICIENT_STOCK`. A jump in
  `INTERNAL_ERROR` or `CONCURRENT_UPDATE` is worth an alert.

## Lombok and MapStruct

Both run at compile time and generate plain Java you can open in
`target/generated-sources/annotations`. When a mapping looks wrong, read the generated `…MapperImpl`
or step into it in the debugger.

**Lombok** is only used for code nobody needs to read: `@Getter`, `@RequiredArgsConstructor`
(constructor injection), `@NoArgsConstructor(access = PROTECTED)` (required by JPA) and `@Slf4j`.
[`lombok.config`](lombok.config) makes the build fail on `@Data`, `@EqualsAndHashCode`, `@ToString` and
`@Value`. On entities they walk lazy relations, which can load half the database or recurse forever.
Use a record for value types.

**MapStruct** turns entities into response records. There is one mapper per entity
(`product/mapper/ProductMapper`), and they all share
[`MapStructConfig`](src/main/java/valleysoft/minibasket/common/mapping/MapStructConfig.java):

- Mappers are Spring beans, so services inject them like any other dependency.
- **A response field nobody mapped is a compile error** (`unmappedTargetPolicy = ERROR`). Add
  `logoUrl` to `BrandResponse` and the build fails until `BrandMapper` knows where it comes from.
- Same-named fields map by themselves. Everything else is one `@Mapping` line:
  `@Mapping(target = "brandName", source = "brand.name")`, which is null-safe when there is no brand.
- Values computed by the entity are getters (`getEffectivePrice()`, `isInStock()`), so MapStruct
  can read them like fields. Business rules stay in the entity, not in the mapper.
- Mappers only go one way, entity to response. Requests become entities through the entity's
  constructor or methods (`Product.create(request, …)`, `order.ship(…)`), so the entity checks its own rules.
- Avoid `expression = "java(…)"`. It is code in a string, and the compiler can't help you there.
  If a value needs logic, put it in the entity (a getter) or in a `default` method like `CartMapper.toResponse`.

In unit tests, use the real generated mapper (`new OrderMapperImpl()`), not a mock.
`ProductMapperTest` shows what is worth testing: missing relations and computed values.

## Design decisions worth knowing

- **Stock can't be oversold.** Checkout takes stock with one SQL `UPDATE … WHERE stock_quantity >= :qty`.
  If two customers race for the last item, only one update succeeds (`StockService.take`).
- **Orders keep their own copy** of prices and the shipping address, so editing a product or an
  address later never changes a past order.
- **Two layers of admin protection:** `/api/v1/admin/**` needs the `ADMIN` role (`SecurityConfig`),
  and each admin service method also checks a specific permission such as `CATALOG_MANAGE`
  (`@PreAuthorize`). Permissions come from the `role_permissions` table.
- **Other people's data returns 404, not 403**, so ids of other customers' orders or addresses
  can't be discovered.
- **The user is reloaded on every request**, so a disabled account loses access immediately even
  if its token hasn't expired.
- **Optimistic locking on orders and products** (`@Version`): if two changes based on the same copy
  meet, the second gets a 409 instead of silently overwriting the first. For products the admin form
  sends back the `version` it loaded, and the checkout's stock `UPDATE` raises the version too, so
  saving an old form can never undo stock that customers bought meanwhile (`ProductConcurrencyIT`).
- **Demo-only shortcuts:** payment is a single "mark as paid" call, and there is no refresh token
  (access tokens last 1 hour).
