package valleysoft.minibasket.auth.entity;

import java.time.Duration;

public enum VerificationTokenType {

    EMAIL_VERIFICATION(Duration.ofHours(24)),
    PASSWORD_RESET(Duration.ofHours(1));

    private final Duration validFor;

    VerificationTokenType(Duration validFor) {
        this.validFor = validFor;
    }

    public Duration validFor() {
        return validFor;
    }
}
