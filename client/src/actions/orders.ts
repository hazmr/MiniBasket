"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { api, attempt } from "@/lib/api";
import type { ActionState } from "@/lib/action-state";
import { text } from "@/lib/format";
import type { Order } from "@/lib/types";

export async function placeOrder(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.post<Order>("/orders", {
      addressId: Number(text(formData, "addressId")),
      shippingMethodId: Number(text(formData, "shippingMethodId")),
      couponCode: text(formData, "couponCode") || null,
    }),
  );
  if (!result.ok) return { error: result.error };

  revalidatePath("/", "layout");
  redirect(`/orders/${result.data.id}?notice=placed`);
}

export async function payOrder(orderId: number): Promise<ActionState> {
  const result = await attempt(() => api.post(`/orders/${orderId}/pay`));
  if (!result.ok) return { error: result.error };

  redirect(`/orders/${orderId}?notice=paid`);
}

export async function cancelOrder(orderId: number): Promise<ActionState> {
  const result = await attempt(() => api.post(`/orders/${orderId}/cancel`));
  if (!result.ok) return { error: result.error };

  redirect(`/orders/${orderId}?notice=cancelled`);
}
