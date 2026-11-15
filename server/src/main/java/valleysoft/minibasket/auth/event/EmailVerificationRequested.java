package valleysoft.minibasket.auth.event;

public record EmailVerificationRequested(String email, String token) {
}
