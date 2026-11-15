"use server";

import { refresh } from "next/cache";
import { redirect } from "next/navigation";
import { api, attempt } from "@/lib/api";
import type { ActionState } from "@/lib/action-state";
import { hasSession } from "@/lib/session";

export async function addToWishlist(productId: number): Promise<ActionState> {
  if (!(await hasSession())) redirect(`/login?next=/products/${productId}`);

  const result = await attempt(() => api.post("/wishlist/items", { productId }));
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Saved to your wishlist" };
}

export async function removeFromWishlist(productId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/wishlist/items/${productId}`));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}
