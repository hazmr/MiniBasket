package valleysoft.minibasket.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import valleysoft.minibasket.user.entity.User;

@Entity
@Table(name = "verification_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationToken {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationTokenType type;

    @Column(nullable = false)
    private Instant expiresAt;

    public static VerificationToken issue(User user, VerificationTokenType type) {
        var verificationToken = new VerificationToken();
        verificationToken.user = user;
        verificationToken.type = type;
        verificationToken.token = randomToken();
        verificationToken.expiresAt = Instant.now().plus(type.validFor());
        return verificationToken;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
