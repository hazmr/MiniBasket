import { Heart, ShoppingCart } from "lucide-react";
import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api";
import { getCurrentUser } from "@/lib/session";
import type { Cart } from "@/lib/types";
import { UserMenu } from "./user-menu";

export async function UserNav() {
  const user = await getCurrentUser();

  if (!user) {
    return (
      <nav className="flex items-center gap-2">
        <Button variant="ghost" asChild>
          <Link href="/login">Log in</Link>
        </Button>
        <Button asChild>
          <Link href="/register">Sign up</Link>
        </Button>
      </nav>
    );
  }

  const cart = await api.get<Cart>("/cart");

  return (
    <nav className="flex items-center gap-1">
      <Button variant="ghost" size="icon" asChild>
        <Link href="/wishlist" aria-label="Wishlist">
          <Heart />
        </Link>
      </Button>
      <Button variant="ghost" asChild>
        <Link href="/cart" aria-label={`Cart, ${cart.totalQuantity} items`}>
          <ShoppingCart data-icon="inline-start" />
          Cart
          <Badge className="ml-0.5 min-w-5 px-1.5 tabular-nums">{cart.totalQuantity}</Badge>
        </Link>
      </Button>
      <UserMenu email={user.email} isAdmin={user.role === "ADMIN"} />
    </nav>
  );
}
