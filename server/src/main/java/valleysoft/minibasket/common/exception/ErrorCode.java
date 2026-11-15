package valleysoft.minibasket.common.exception;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;

public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request couldn't be read"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Some fields are invalid"),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "You can't sort by {property}"),
    UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "Only JPEG, PNG and WebP images can be uploaded"),
    IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "Images can be at most {maxMegabytes} MB"),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "This upload doesn't belong to this product"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A valid bearer token is required"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email or password is wrong"),
    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "This account has been disabled"),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "Please verify your email before logging in"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You don't have permission to do this"),

    NOT_FOUND(HttpStatus.NOT_FOUND, "This address doesn't exist"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Address {id} not found"),
    BRAND_NOT_FOUND(HttpStatus.NOT_FOUND, "Brand {id} not found"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Cart item {id} not found"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category {id} not found"),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "Coupon {id} not found"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order {id} not found"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product {id} not found"),
    PRODUCT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Product image {id} not found"),
    SHIPPING_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "Shipping method {id} not found"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User {id} not found"),
    WISHLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Product {id} isn't in your wishlist"),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "This HTTP method isn't allowed here"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "This response format isn't available"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "This content type isn't supported"),
    CONTENT_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "The request is too large"),

    EMAIL_TAKEN(HttpStatus.CONFLICT, "An account with this email already exists"),
    BRAND_EXISTS(HttpStatus.CONFLICT, "A brand named “{name}” already exists"),
    SKU_EXISTS(HttpStatus.CONFLICT, "A product with SKU {sku} already exists"),
    COUPON_EXISTS(HttpStatus.CONFLICT, "Coupon {code} already exists"),
    PRODUCT_CHANGED(HttpStatus.CONFLICT,
            "This product changed since you opened it (for example, stock was sold). Reload the page and try again."),
    CONCURRENT_UPDATE(HttpStatus.CONFLICT, "Someone else changed this at the same time. Reload the page and try again."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "This conflicts with existing data, for example something that is still in use"),

    INVALID_TOKEN(HttpStatus.UNPROCESSABLE_CONTENT, "The link or code is invalid or has expired"),
    CART_EMPTY(HttpStatus.UNPROCESSABLE_CONTENT, "Your cart is empty"),
    INSUFFICIENT_STOCK(HttpStatus.UNPROCESSABLE_CONTENT, "Only {available} of “{productName}” left in stock"),
    INVALID_COUPON(HttpStatus.UNPROCESSABLE_CONTENT, "Coupon {code} doesn't exist"),
    COUPON_EXPIRED(HttpStatus.UNPROCESSABLE_CONTENT, "Coupon {code} has expired"),
    INVALID_ORDER_STATUS(HttpStatus.UNPROCESSABLE_CONTENT, "Can't {action} order {orderNumber} while it is {status}"),
    STATUS_NOT_SETTABLE(HttpStatus.UNPROCESSABLE_CONTENT,
            "Orders can't be set to {status} by hand. Choose SHIPPED, DELIVERED or CANCELLED."),
    TRACKING_NUMBER_REQUIRED(HttpStatus.UNPROCESSABLE_CONTENT, "A tracking number is required to ship an order"),
    INVALID_DISCOUNT(HttpStatus.UNPROCESSABLE_CONTENT, "The sale price must be lower than the price"),
    PERCENTAGE_TOO_HIGH(HttpStatus.UNPROCESSABLE_CONTENT, "A percentage discount can't be more than 100%"),
    CATEGORY_CYCLE(HttpStatus.UNPROCESSABLE_CONTENT, "A category can't be placed inside itself or its own subcategory"),
    IMAGE_NOT_UPLOADED(HttpStatus.UNPROCESSABLE_CONTENT, "The image hasn't finished uploading. Please try again."),
    INVALID_IMAGE(HttpStatus.UNPROCESSABLE_CONTENT, "The uploaded file isn't a valid image"),

    TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Try again in {minutes} minutes."),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "This is temporarily unavailable. Please try again in a moment.");

    private final HttpStatus status;
    private final String template;
    private final Set<String> params;

    ErrorCode(HttpStatus status, String template) {
        this.status = status;
        this.template = template;
        this.params = placeholdersOf(template);
    }

    public HttpStatus status() {
        return status;
    }

    public String template() {
        return template;
    }

    public Set<String> params() {
        return params;
    }

    public String render(Map<String, ?> values) {
        Matcher matcher = Placeholder.PATTERN.matcher(template);
        StringBuilder message = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(message, Matcher.quoteReplacement(String.valueOf(values.get(matcher.group(1)))));
        }
        return matcher.appendTail(message).toString();
    }

    public static ErrorCode forHttpStatus(int status) {
        for (ErrorCode code : new ErrorCode[] {BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, METHOD_NOT_ALLOWED,
                NOT_ACCEPTABLE, UNSUPPORTED_MEDIA_TYPE, CONTENT_TOO_LARGE, INTERNAL_ERROR}) {
            if (code.status.value() == status) {
                return code;
            }
        }
        return status >= 500 ? INTERNAL_ERROR : BAD_REQUEST;
    }

    private static Set<String> placeholdersOf(String template) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = Placeholder.PATTERN.matcher(template);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return Set.copyOf(names);
    }

    private static final class Placeholder {
        static final Pattern PATTERN = Pattern.compile("\\{(\\w+)}");
    }
}
