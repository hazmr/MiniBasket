
export type Role = "CUSTOMER" | "ADMIN";

export type User = {
  id: number;
  email: string;
  role: Role;
  permissions: string[];
  emailVerified: boolean;
  createdAt: string;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
};

export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export type Category = { id: number; name: string; parentId: number | null };

export type Brand = { id: number; name: string };

export type ProductSummary = {
  id: number;
  sku: string;
  name: string;
  price: number;
  discountPrice: number | null;
  inStock: boolean;
  categoryName: string;
  brandName: string | null;
  imageUrl: string | null;
};

export type ProductImage = { id: number; imageUrl: string };

export type ImageUpload = { key: string; uploadUrl: string; expiresAt: string };

export type Product = {
  id: number;
  sku: string;
  name: string;
  price: number;
  discountPrice: number | null;
  stockQuantity: number;
  attributes: Record<string, unknown>;
  category: Category;
  brand: Brand | null;
  images: ProductImage[];
  version: number;
};

export type CartItem = {
  id: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type Cart = { items: CartItem[]; totalQuantity: number; subtotal: number };

export type WishlistItem = {
  productId: number;
  productName: string;
  price: number;
  inStock: boolean;
  addedAt: string;
};

export type Address = { id: number; street: string; city: string; defaultAddress: boolean };

export type ShippingMethod = { id: number; name: string; fee: number };

export type DiscountType = "PERCENTAGE" | "FIXED";

export type Coupon = {
  id: number;
  code: string;
  discountType: DiscountType;
  discountValue: number;
  expiresAt: string | null;
  expired: boolean;
};

export type OrderStatus = "PENDING" | "CONFIRMED" | "SHIPPED" | "DELIVERED" | "CANCELLED";

export type PaymentStatus = "UNPAID" | "PAID" | "REFUNDED";

export type OrderSummary = {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  total: number;
  createdAt: string;
};

export type OrderItem = {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type Order = {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  items: OrderItem[];
  subtotal: number;
  couponCode: string | null;
  discountAmount: number;
  shippingMethod: string;
  shippingFee: number;
  total: number;
  shippingStreet: string;
  shippingCity: string;
  trackingNumber: string | null;
  createdAt: string;
};
