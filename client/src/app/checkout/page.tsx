import { MapPin, ShoppingCart } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { addAddress } from "@/actions/account";
import { placeOrder } from "@/actions/orders";
import { ActionForm } from "@/components/action-form";
import { AddressFields } from "@/components/address-fields";
import { EmptyState } from "@/components/empty-state";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldContent, FieldDescription, FieldGroup, FieldLabel, FieldTitle } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { api } from "@/lib/api";
import { money } from "@/lib/format";
import { requireUser } from "@/lib/session";
import type { Address, Cart, ShippingMethod } from "@/lib/types";

export default function CheckoutPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Checkout" />
      <Suspense fallback={<Loading />}>
        <Checkout />
      </Suspense>
    </div>
  );
}

async function Checkout() {
  await requireUser("/checkout");
  const [cart, addresses, shippingMethods] = await Promise.all([
    api.get<Cart>("/cart"),
    api.get<Address[]>("/addresses"),
    api.get<ShippingMethod[]>("/shipping-methods"),
  ]);

  if (cart.items.length === 0) {
    return (
      <EmptyState icon={ShoppingCart} title="Nothing to check out" description="Your cart is empty.">
        <Button asChild>
          <Link href="/">Back to the shop</Link>
        </Button>
      </EmptyState>
    );
  }

  if (addresses.length === 0) {
    return (
      <Card className="max-w-lg">
        <CardHeader>
          <CardTitle>Where should we ship your order?</CardTitle>
          <CardDescription>You can add more addresses later in your account.</CardDescription>
        </CardHeader>
        <CardContent>
          <ActionForm action={addAddress} className="flex flex-col gap-5">
            <FieldGroup className="gap-4">
              <AddressFields />
            </FieldGroup>
            <input type="hidden" name="defaultAddress" value="on" />
            <SubmitButton className="w-fit">Save address</SubmitButton>
          </ActionForm>
        </CardContent>
      </Card>
    );
  }

  const defaultAddress = addresses.find((address) => address.defaultAddress) ?? addresses[0];

  return (
    <ActionForm action={placeOrder} className="grid gap-6 lg:grid-cols-3">
      <div className="flex flex-col gap-6 lg:col-span-2">
        <Card>
          <CardHeader>
            <CardTitle>Shipping address</CardTitle>
            <CardDescription>
              <Link href="/account" className="text-primary hover:underline">Add or edit addresses</Link>
            </CardDescription>
          </CardHeader>
          <CardContent>
            <RadioGroup name="addressId" defaultValue={String(defaultAddress.id)} required>
              {addresses.map((address) => (
                <FieldLabel key={address.id} htmlFor={`address-${address.id}`}>
                  <Field orientation="horizontal">
                    <FieldContent>
                      <FieldTitle><MapPin className="size-4 text-muted-foreground" />{address.street}</FieldTitle>
                      <FieldDescription>{address.city}</FieldDescription>
                    </FieldContent>
                    <RadioGroupItem value={String(address.id)} id={`address-${address.id}`} />
                  </Field>
                </FieldLabel>
              ))}
            </RadioGroup>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Delivery</CardTitle>
          </CardHeader>
          <CardContent>
            <RadioGroup name="shippingMethodId" defaultValue={String(shippingMethods[0]?.id)} required>
              {shippingMethods.map((method) => (
                <FieldLabel key={method.id} htmlFor={`shipping-${method.id}`}>
                  <Field orientation="horizontal">
                    <FieldContent>
                      <FieldTitle>{method.name}</FieldTitle>
                      <FieldDescription>{money(method.fee)}</FieldDescription>
                    </FieldContent>
                    <RadioGroupItem value={String(method.id)} id={`shipping-${method.id}`} />
                  </Field>
                </FieldLabel>
              ))}
            </RadioGroup>
          </CardContent>
        </Card>
      </div>

      <Card className="h-fit">
        <CardHeader>
          <CardTitle>Order summary</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4 text-sm">
          <ul className="flex flex-col gap-2">
            {cart.items.map((item) => (
              <li key={item.id} className="flex justify-between gap-2">
                <span className="text-muted-foreground">{item.quantity} × {item.productName}</span>
                <span>{money(item.lineTotal)}</span>
              </li>
            ))}
          </ul>
          <Separator />
          <div className="flex justify-between text-base font-semibold">
            <span>Subtotal</span>
            <span>{money(cart.subtotal)}</span>
          </div>
          <Field>
            <FieldLabel htmlFor="couponCode">Coupon code</FieldLabel>
            <Input id="couponCode" name="couponCode" placeholder="e.g. WELCOME10" className="font-mono uppercase" />
          </Field>
          <SubmitButton size="lg" className="w-full">Place order</SubmitButton>
          <p className="text-xs text-muted-foreground">Shipping and discounts are added when the order is placed.</p>
        </CardContent>
      </Card>
    </ActionForm>
  );
}
