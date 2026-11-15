"use server";

import { refresh } from "next/cache";
import { redirect } from "next/navigation";
import { api, attempt } from "@/lib/api";
import type { ActionState } from "@/lib/action-state";
import { optionalNumber, text } from "@/lib/format";
import type { ImageUpload, Product } from "@/lib/types";

function productFromForm(formData: FormData) {
  const attributesText = text(formData, "attributes");
  let attributes: Record<string, unknown> = {};
  if (attributesText) {
    try {
      attributes = JSON.parse(attributesText);
    } catch {
      return { error: 'Attributes must be valid JSON, for example {"color": "black"}' };
    }
  }

  return {
    product: {
      sku: text(formData, "sku"),
      name: text(formData, "name"),
      price: optionalNumber(formData, "price"),
      discountPrice: optionalNumber(formData, "discountPrice"),
      stockQuantity: optionalNumber(formData, "stockQuantity"),
      categoryId: optionalNumber(formData, "categoryId"),
      brandId: optionalNumber(formData, "brandId"),
      attributes,
      version: optionalNumber(formData, "version"),
    },
  };
}

export async function createProduct(_: ActionState, formData: FormData): Promise<ActionState> {
  const { product, error } = productFromForm(formData);
  if (error) return { error };

  const result = await attempt(() => api.post<Product>("/admin/products", product));
  if (!result.ok) return { error: result.error };

  redirect(`/admin/products/${result.data.id}`);
}

export async function updateProduct(productId: number, _: ActionState, formData: FormData): Promise<ActionState> {
  const { product, error } = productFromForm(formData);
  if (error) return { error };

  const result = await attempt(() => api.put(`/admin/products/${productId}`, product));
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Product saved" };
}

export async function deleteProduct(productId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/admin/products/${productId}`));
  if (!result.ok) return { error: result.error };

  redirect("/admin/products");
}

export async function requestImageUpload(
  productId: number,
  contentType: string,
  size: number,
): Promise<ImageUpload | { error: string }> {
  const result = await attempt(() =>
    api.post<ImageUpload>(`/admin/products/${productId}/images/upload-url`, { contentType, size }),
  );
  return result.ok ? result.data : { error: result.error };
}

export async function attachProductImage(productId: number, key: string): Promise<ActionState> {
  const result = await attempt(() => api.post(`/admin/products/${productId}/images`, { key }));
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Image added" };
}

export async function removeProductImage(productId: number, imageId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/admin/products/${productId}/images/${imageId}`));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}

export async function createCategory(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.post("/admin/categories", { name: text(formData, "name"), parentId: optionalNumber(formData, "parentId") }),
  );
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Category added" };
}

export async function deleteCategory(categoryId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/admin/categories/${categoryId}`));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}

export async function createBrand(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() => api.post("/admin/brands", { name: text(formData, "name") }));
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Brand added" };
}

export async function deleteBrand(brandId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/admin/brands/${brandId}`));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}

export async function createCoupon(_: ActionState, formData: FormData): Promise<ActionState> {
  const expiresAt = text(formData, "expiresAt");
  const result = await attempt(() =>
    api.post("/admin/coupons", {
      code: text(formData, "code"),
      discountType: text(formData, "discountType"),
      discountValue: optionalNumber(formData, "discountValue"),
      expiresAt: expiresAt ? new Date(expiresAt).toISOString() : null,
    }),
  );
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Coupon created" };
}

export async function deleteCoupon(couponId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/admin/coupons/${couponId}`));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}

export async function createShippingMethod(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.post("/admin/shipping-methods", { name: text(formData, "name"), fee: optionalNumber(formData, "fee") }),
  );
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Shipping method added" };
}

export async function deleteShippingMethod(shippingMethodId: number): Promise<ActionState> {
  const result = await attempt(() => api.delete(`/admin/shipping-methods/${shippingMethodId}`));
  if (!result.ok) return { error: result.error };

  refresh();
  return {};
}

export async function updateOrderStatus(orderId: number, _: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.patch(`/admin/orders/${orderId}/status`, {
      status: text(formData, "status"),
      trackingNumber: text(formData, "trackingNumber") || null,
    }),
  );
  if (!result.ok) return { error: result.error };

  refresh();
  return { message: "Order updated" };
}
