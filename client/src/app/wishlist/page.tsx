import { Heart, ShoppingCart, Trash2 } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { addToCart } from "@/actions/cart";
import { removeFromWishlist } from "@/actions/wishlist";
import { ActionButton } from "@/components/action-button";
import { ActionForm } from "@/components/action-form";
import { EmptyState } from "@/components/empty-state";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { api } from "@/lib/api";
import { money } from "@/lib/format";
import { requireUser } from "@/lib/session";
import type { WishlistItem } from "@/lib/types";

export default function WishlistPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Your wishlist" description="Products you saved for later." />
      <Suspense fallback={<Loading />}>
        <Wishlist />
      </Suspense>
    </div>
  );
}

async function Wishlist() {
  await requireUser("/wishlist");
  const items = await api.get<WishlistItem[]>("/wishlist");

  if (items.length === 0) {
    return (
      <EmptyState icon={Heart} title="Nothing saved yet" description="Tap “Save to wishlist” on a product to keep it here.">
        <Button asChild>
          <Link href="/">Browse products</Link>
        </Button>
      </EmptyState>
    );
  }

  return (
    <Card className="py-0">
      <ul className="divide-y">
        {items.map((item) => (
          <li key={item.productId} className="flex flex-wrap items-center gap-4 p-4">
            <div className="min-w-40 flex-1">
              <Link href={`/products/${item.productId}`} className="font-medium hover:underline">{item.productName}</Link>
              <p className="flex items-center gap-2 text-sm text-muted-foreground">
                {money(item.price)}
                {!item.inStock && <Badge variant="secondary">Sold out</Badge>}
              </p>
            </div>
            <ActionForm action={addToCart} errorsAs="toast" className="contents">
              <input type="hidden" name="productId" value={item.productId} />
              <SubmitButton disabled={!item.inStock}>
                <ShoppingCart data-icon="inline-start" />
                Add to cart
              </SubmitButton>
            </ActionForm>
            <ActionButton action={removeFromWishlist.bind(null, item.productId)} variant="ghost" size="icon" aria-label={`Remove ${item.productName}`}>
              <Trash2 />
            </ActionButton>
          </li>
        ))}
      </ul>
    </Card>
  );
}
