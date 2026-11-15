import { Search, ShoppingBasket } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { InputGroup, InputGroupAddon, InputGroupInput } from "@/components/ui/input-group";
import { Skeleton } from "@/components/ui/skeleton";
import { ThemeToggle } from "./theme-toggle";
import { UserNav } from "./user-nav";

export function Header() {
  return (
    <header className="sticky top-0 z-40 border-b bg-background/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-4 gap-y-3 px-4 py-3">
        <Link href="/" className="flex items-center gap-2 font-heading text-lg font-semibold">
          <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <ShoppingBasket className="size-4" />
          </span>
          MiniBasket
        </Link>

        <form action="/" className="order-last w-full md:order-none md:w-auto md:flex-1">
          <InputGroup className="md:max-w-md">
            <InputGroupAddon>
              <Search />
            </InputGroupAddon>
            <InputGroupInput name="name" placeholder="Search products…" aria-label="Search products" />
          </InputGroup>
        </form>

        <div className="ml-auto flex items-center gap-1">
          <ThemeToggle />
          <Suspense fallback={<Skeleton className="h-8 w-40" />}>
            <UserNav />
          </Suspense>
        </div>
      </div>
    </header>
  );
}
