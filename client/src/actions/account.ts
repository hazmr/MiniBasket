"use server";

import { refresh } from "next/cache";
import { api, attempt } from "@/lib/api";
import type { ActionState } from "@/lib/action-state";
import { text } from "@/lib/format";
import type { Address } from "@/lib/types";

export async function addAddress(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.post("/addresses", {
      street: text(formData, "street"),
      city: text(formData, "city"),
      defaultAddress: formData.get("defaultAddress") === "on",
    }),
  );
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Address saved" };
}

export async function makeDefaultAddress(address: Address): Promise<ActionState> {
  const result = await attempt(() =>
    api.put(`/addresses/${address.id}`, { street: address.street, city: address.city, defaultAddress: true }),
  );
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}

export async function deleteAddress(addressId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/addresses/${addressId}`));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}
