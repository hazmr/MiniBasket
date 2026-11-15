import type { components } from "./api-schema";

export type ErrorCode = components["schemas"]["ErrorCode"];
export type Problem = components["schemas"]["Problem"];
type Violation = components["schemas"]["Violation"];

type Params = Record<string, unknown>;

const messages: Record<ErrorCode, string> = {
  BAD_REQUEST: "Something about that request wasn't right. Please try again.",
  VALIDATION_FAILED: "Please check the highlighted fields.",
  INVALID_SORT: "That sort order isn't available.",
  UNSUPPORTED_IMAGE_TYPE: "Only JPEG, PNG and WebP images can be uploaded.",
  IMAGE_TOO_LARGE: "Images can be at most {maxMegabytes} MB.",
  INVALID_IMAGE_KEY: "That upload doesn't belong to this product. Please upload it again.",

  UNAUTHORIZED: "Please log in to continue.",
  INVALID_CREDENTIALS: "Email or password is wrong.",
  ACCOUNT_DISABLED: "This account has been disabled.",
  EMAIL_NOT_VERIFIED: "Please verify your email before logging in. Check your inbox for the link.",
  FORBIDDEN: "You don't have permission to do this.",

  NOT_FOUND: "We couldn't find that page.",
  ADDRESS_NOT_FOUND: "That address doesn't exist anymore.",
  BRAND_NOT_FOUND: "That brand doesn't exist anymore.",
  CART_ITEM_NOT_FOUND: "That item is no longer in your cart.",
  CATEGORY_NOT_FOUND: "That category doesn't exist anymore.",
  COUPON_NOT_FOUND: "That coupon doesn't exist anymore.",
  ORDER_NOT_FOUND: "We couldn't find that order.",
  PRODUCT_NOT_FOUND: "That product doesn't exist anymore.",
  PRODUCT_IMAGE_NOT_FOUND: "That image doesn't exist anymore.",
  SHIPPING_METHOD_NOT_FOUND: "That shipping method isn't available anymore.",
  USER_NOT_FOUND: "We couldn't find that user.",
  WISHLIST_ITEM_NOT_FOUND: "That product isn't in your wishlist.",

  METHOD_NOT_ALLOWED: "Something about that request wasn't right. Please try again.",
  NOT_ACCEPTABLE: "Something about that request wasn't right. Please try again.",
  UNSUPPORTED_MEDIA_TYPE: "Something about that request wasn't right. Please try again.",
  CONTENT_TOO_LARGE: "That's too much data to send at once.",

  EMAIL_TAKEN: "An account with this email already exists. Try logging in instead.",
  BRAND_EXISTS: "A brand named “{name}” already exists.",
  SKU_EXISTS: "Another product already uses SKU {sku}.",
  COUPON_EXISTS: "Coupon {code} already exists.",
  PRODUCT_CHANGED: "This product changed since you opened it, for example stock was sold. Reload the page and try again.",
  CONCURRENT_UPDATE: "Someone else changed this at the same time. Reload the page and try again.",
  DATA_CONFLICT: "This can't be done because other data depends on it.",

  INVALID_TOKEN: "This link is invalid or has expired. Please request a new one.",
  CART_EMPTY: "Your cart is empty.",
  INSUFFICIENT_STOCK: "Only {available} of “{productName}” left in stock.",
  INVALID_COUPON: "Coupon {code} doesn't exist.",
  COUPON_EXPIRED: "Coupon {code} has expired.",
  INVALID_ORDER_STATUS: "Order {orderNumber} can't be changed now, it is {status}.",
  STATUS_NOT_SETTABLE: "Orders can't be set to {status} by hand.",
  TRACKING_NUMBER_REQUIRED: "Add a tracking number to ship the order.",
  INVALID_DISCOUNT: "The sale price must be lower than the price.",
  PERCENTAGE_TOO_HIGH: "A percentage discount can't be more than 100%.",
  CATEGORY_CYCLE: "A category can't be placed inside itself or one of its subcategories.",
  IMAGE_NOT_UPLOADED: "The image hasn't finished uploading. Please try again.",
  INVALID_IMAGE: "That file isn't a valid image.",

  TOO_MANY_ATTEMPTS: "Too many attempts. Please wait {minutes} min and try again.",

  INTERNAL_ERROR: "Something went wrong on our side. Please try again.",
  SERVICE_UNAVAILABLE: "This is temporarily unavailable. Please try again in a moment.",
};

const constraintMessages: Record<string, (params: Params) => string> = {
  NotBlank: () => "is required",
  NotNull: () => "is required",
  Email: () => "must be a valid email address",
  Size: ({ min, max }) => (Number(min) > 0 ? `must be ${min} to ${max} characters` : `can be at most ${max} characters`),
  Max: ({ value }) => `can be at most ${value}`,
  Positive: () => "must be more than 0",
  PositiveOrZero: () => "can't be negative",
  DecimalMin: ({ value }) => `can't be less than ${value}`,
  Digits: ({ fraction }) => `must be a number with at most ${fraction} decimals`,
};

function fill(template: string, params: Params = {}): string {
  return template.replace(/\{(\w+)}/g, (placeholder, name: string) =>
    name in params ? String(params[name]) : placeholder,
  );
}

function fieldLabel(field: string): string {
  const last = field.split(".").pop()!.replace(/\[\d+]/g, "");
  const words = last.replace(/([A-Z])/g, " $1").toLowerCase();
  return words.charAt(0).toUpperCase() + words.slice(1);
}

function violationMessage(violation: Violation): string {
  const describe = constraintMessages[violation.code];
  return `${fieldLabel(violation.field)} ${describe ? describe(violation.params ?? {}) : violation.message}`;
}

export function messageFor(problem: Problem): string {
  if (problem.violations?.length) {
    return problem.violations.map(violationMessage).join(". ") + ".";
  }
  const message = fill(messages[problem.errorCode] ?? messages.INTERNAL_ERROR, problem.params);
  if (problem.status >= 500 && problem.traceId) return `${message} (reference: ${problem.traceId})`;
  return message;
}
