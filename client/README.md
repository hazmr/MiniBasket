# MiniBasket client

The MiniBasket website: Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4 and
[shadcn/ui](https://ui.shadcn.com) (Radix, "nova" style, Lucide icons).

## Run it

Start the API first (see `../server/README.md`), then:

```bash
npm install
npm run dev     # http://localhost:3000
```

`API_URL` in `.env.local` says where the API is (default `http://localhost:8080`).
It is only read on the server, never sent to the browser.

Other commands: `npm run build` (production build + type check) and `npm run lint`.

## How it works

Pages are **Server Components**: they run on the Next.js server, call the API there and send
finished HTML to the browser. Every change (log in, add to cart, place an order...) is a
**Server Action**: a function in `src/actions/` that the form calls directly.

```
src/
├── app/          one folder per URL: page.tsx is the page, e.g. app/cart/page.tsx → /cart
│   └── admin/    admin pages; the API rejects them for anyone who isn't an admin
├── actions/      Server Actions: every write to the API (auth, cart, orders, admin...)
├── components/   our own UI pieces (header, product card, order details...)
│   └── ui/       shadcn components: copied into the project, so we own and can edit them
└── lib/
    ├── api.ts             the only place that calls the Spring Boot API
    ├── error-messages.ts  the text users see for each API errorCode
    ├── api-schema.d.ts    generated from the API docs (npm run generate:api), don't edit
    ├── session.ts         login cookie, current user, requireUser() / requireAdmin()
    └── types.ts           TypeScript shapes of the API's JSON responses
```

### Login

`actions/auth.ts` posts the email and password to the API and stores the returned JWT in an
**httpOnly cookie**. JavaScript in the browser can't read that cookie, so a malicious script can't
steal the token. `lib/api.ts` reads the cookie on the server and adds `Authorization: Bearer …`
to every API call. If the API answers 401 (token expired), the user is logged out and sent to
the login page.

### Forms and errors

Forms use `components/action-form.tsx`. It runs the Server Action, shows the error message and
keeps what the user typed when something fails. Server Actions return `{ error }` or `{ message }`
instead of throwing, so expected problems never crash the page.

The text the user sees comes from **`src/lib/error-messages.ts`**, not from the API. The API sends
an `errorCode` and its `params` (`INSUFFICIENT_STOCK`, `{available: 2, productName: "Nova X1"}`)
and the catalog turns that into "Only 2 of “Nova X1” left in stock." Errors on our side add
the request's `traceId` as a reference the user can quote.

The `ErrorCode` type is generated from the API docs into `src/lib/api-schema.d.ts`. The catalog is a
`Record<ErrorCode, string>`, so when the backend adds a code, the build fails until it has a
message. After a backend change, with the server running:

```bash
npm run generate:api
```

A second language would be a second catalog of the same type.

### Styling

Colors come from CSS variables in `src/app/globals.css` (`--primary` is the green brand color),
so changing the theme means editing one file. Add a new shadcn component with
`npx shadcn@latest add <name>`. Forms use the `Field` and `NativeSelect` components because
they post plain form data, which is exactly what Server Actions receive.

### Why every page has `<Suspense>`

The project uses Next.js **Cache Components** (`next.config.ts`). Anything that reads the
login cookie or the URL can only be known at request time, so it must sit inside `<Suspense>`.
That's why pages look like this:

```tsx
export default function CartPage() {
  return (
    <Suspense fallback={<Loading />}>
      <CartContents />   {/* reads the cookie and calls the API */}
    </Suspense>
  );
}
```

The static parts (header, titles) show instantly and the data streams in right after.
