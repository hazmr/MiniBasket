package valleysoft.minibasket.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN = "access";

    private final SecretKey signingKey;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    public String generateAccessToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenExpiration())))
                .signWith(signingKey)
                .compact();
    }

    public long accessTokenExpirationSeconds() {
        return properties.accessTokenExpiration().toSeconds();
    }

    public String validateAndGetEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!ACCESS_TOKEN.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                || claims.getExpiration() == null
                || claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new JwtException("Invalid access token claims");
        }
        return claims.getSubject();
    }
}
