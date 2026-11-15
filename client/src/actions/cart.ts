"use server";

import { refresh } from "next/cache";
import { redirect } from "next/navigation";
import { api, attempt } from "@/lib/api";
import type { ActionState } from "@/lib/action-state";
import { text } from "@/lib/format";
import { hasSession, safeRedirectPath } from "@/lib/session";

export async function addToCart(_: ActionState, formData: FormData): Promise<ActionState> {
  if (!(await hasSession())) {
    redirect(`/login?next=${encodeURIComponent(safeRedirectPath(formData.get("returnTo")))}`);
  }

  const result = await attempt(() =>
    api.post("/cart/items", {
      productId: Number(text(formData, "productId")),
      quantity: Number(text(formData, "quantity") || 1),
    }),
  );
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Added to your cart", href: "/cart", hrefLabel: "View cart" };
}

export async function updateCartItem(itemId: number, _: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.put(`/cart/items/${itemId}`, { quantity: Number(text(formData, "quantity")) }),
  );
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}

export async function removeCartItem(itemId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/cart/items/${itemId}`));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}

export async function clearCart(): Promise<ActionState> {
  const result = await attempt(() => api.delete("/cart"));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}
