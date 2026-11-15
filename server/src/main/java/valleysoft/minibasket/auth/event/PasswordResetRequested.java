package valleysoft.minibasket.auth.event;

public record PasswordResetRequested(String email, String token) {
}
