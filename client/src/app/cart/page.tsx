import { Minus, Plus, ShoppingCart, Trash2 } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { clearCart, removeCartItem, updateCartItem } from "@/actions/cart";
import { ActionButton } from "@/components/action-button";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { ActionForm } from "@/components/action-form";
import { EmptyState } from "@/components/empty-state";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { api } from "@/lib/api";
import { money } from "@/lib/format";
import { requireUser } from "@/lib/session";
import type { Cart } from "@/lib/types";

export default function CartPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Your cart" />
      <Suspense fallback={<Loading />}>
        <CartContents />
      </Suspense>
    </div>
  );
}

async function CartContents() {
  await requireUser("/cart");
  const cart = await api.get<Cart>("/cart");

  if (cart.items.length === 0) {
    return (
      <EmptyState icon={ShoppingCart} title="Your cart is empty" description="Products you add will show up here.">
        <Button asChild>
          <Link href="/">Start shopping</Link>
        </Button>
      </EmptyState>
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="py-0 lg:col-span-2">
        <ul className="divide-y">
          {cart.items.map((item) => (
            <li key={item.id} className="flex flex-wrap items-center gap-4 p-4">
              <div className="min-w-40 flex-1">
                <Link href={`/products/${item.productId}`} className="font-medium hover:underline">
                  {item.productName}
                </Link>
                <p className="text-sm text-muted-foreground">{money(item.unitPrice)} each</p>
              </div>
              <div className="flex items-center rounded-lg border bg-background">
                <ActionForm action={updateCartItem.bind(null, item.id)} errorsAs="toast" className="contents">
                  <input type="hidden" name="quantity" value={item.quantity - 1} />
                  <SubmitButton variant="ghost" size="icon-sm" disabled={item.quantity <= 1} aria-label={`One less ${item.productName}`}>
                    <Minus />
                  </SubmitButton>
                </ActionForm>
                <span className="w-8 text-center text-sm tabular-nums" aria-label={`Quantity of ${item.productName}`}>{item.quantity}</span>
                <ActionForm action={updateCartItem.bind(null, item.id)} errorsAs="toast" className="contents">
                  <input type="hidden" name="quantity" value={item.quantity + 1} />
                  <SubmitButton variant="ghost" size="icon-sm" aria-label={`One more ${item.productName}`}>
                    <Plus />
                  </SubmitButton>
                </ActionForm>
              </div>
              <p className="w-24 text-right font-medium tabular-nums">{money(item.lineTotal)}</p>
              <ActionButton action={removeCartItem.bind(null, item.id)} variant="ghost" size="icon" aria-label={`Remove ${item.productName}`}>
                <Trash2 />
              </ActionButton>
            </li>
          ))}
        </ul>
      </Card>

      <Card className="h-fit">
        <CardHeader>
          <CardTitle>Order summary</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Items</span>
            <span>{cart.totalQuantity}</span>
          </div>
          <Separator />
          <div className="flex justify-between text-base font-semibold">
            <span>Subtotal</span>
            <span>{money(cart.subtotal)}</span>
          </div>
          <p className="text-xs text-muted-foreground">Shipping and coupons are applied at checkout.</p>
        </CardContent>
        <CardFooter className="flex-col gap-2">
          <Button size="lg" className="w-full" asChild>
            <Link href="/checkout">Go to checkout</Link>
          </Button>
          <ConfirmActionButton
            action={clearCart}
            variant="ghost"
            className="w-full"
            title="Empty your cart?"
            description="All items will be removed from your cart."
            confirmLabel="Empty cart"
          >
            Empty cart
          </ConfirmActionButton>
        </CardFooter>
      </Card>
    </div>
  );
}
