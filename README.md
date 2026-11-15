# MiniBasket

A small online shop, built as a demo. Customers browse products, fill a cart, check out with a
coupon and pay; admins manage the catalog, coupons, shipping methods and orders.

```
MiniBasket/
├── server/   Spring Boot 4 API (Java 25, PostgreSQL 18, Redis) → server/README.md
└── client/   Next.js 16 website (React 19, Tailwind)           → client/README.md
```

## Run it

You need Java 25, Node.js 20+ and Docker.

```bash
# terminal 1: the API on http://localhost:8080 (also starts Postgres, Redis, MinIO and Mailpit in Docker)
cd server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# terminal 2: the website on http://localhost:3000
cd client
npm install
npm run dev
```

Open <http://localhost:3000> and log in with a demo account:

| Account | Password |
|---|---|
| `customer@minibasket.dev` | `Customer123!` |
| `admin@minibasket.dev` | `Admin123!` |

Coupon codes to try at checkout: `WELCOME10` (10% off) and `FIVEOFF` (5.00 off).

Locally, emails (sign-up verification, password reset) land in **Mailpit** at <http://localhost:8026>,
and product images live in **MinIO**. To use Cloudflare R2 and Gmail instead, see
`server/README.md` → "Images and email".

## How the two parts talk

The browser only ever talks to the Next.js server. Next.js calls the Spring Boot API from the
server side and keeps the login token in an httpOnly cookie, so browser JavaScript never sees it
and the API needs no CORS setup. The one exception is image uploads (below).

```
Browser ──► Next.js (client/, port 3000) ──► Spring Boot API (server/, port 8080) ──► PostgreSQL
   │
   └──► MinIO / R2: admin image uploads go straight to the bucket, with a signed link from the API
```
