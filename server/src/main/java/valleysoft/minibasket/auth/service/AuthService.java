package valleysoft.minibasket.auth.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.auth.dto.AuthResponse;
import valleysoft.minibasket.auth.dto.LoginRequest;
import valleysoft.minibasket.auth.dto.RegisterRequest;
import valleysoft.minibasket.auth.dto.ResetPasswordRequest;
import valleysoft.minibasket.auth.entity.VerificationToken;
import valleysoft.minibasket.auth.entity.VerificationTokenType;
import valleysoft.minibasket.auth.event.EmailVerificationRequested;
import valleysoft.minibasket.auth.event.PasswordResetRequested;
import valleysoft.minibasket.auth.repository.VerificationTokenRepository;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.security.JwtService;
import valleysoft.minibasket.security.RateLimitProperties;
import valleysoft.minibasket.security.RateLimiter;
import valleysoft.minibasket.user.dto.UserResponse;
import valleysoft.minibasket.user.entity.Role;
import valleysoft.minibasket.user.entity.User;
import valleysoft.minibasket.user.mapper.UserMapper;
import valleysoft.minibasket.user.repository.RoleRepository;
import valleysoft.minibasket.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher events;
    private final UserMapper userMapper;
    private final RateLimiter rateLimiter;
    private final RateLimitProperties rateLimits;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DomainException(ErrorCode.EMAIL_TAKEN);
        }

        Role customerRole = roleRepository.findByName(Role.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role is missing, check the migrations"));
        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()), customerRole));

        VerificationToken token = tokenRepository.save(
                VerificationToken.issue(user, VerificationTokenType.EMAIL_VERIFICATION));
        events.publishEvent(new EmailVerificationRequested(email, token.getToken()));

        return userMapper.toResponse(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = useToken(token, VerificationTokenType.EMAIL_VERIFICATION);
        verificationToken.getUser().verifyEmail();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        rateLimiter.check("login", email, rateLimits.login());

        User user = userRepository.findByEmail(email)
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new DomainException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.isActive()) {
            throw new DomainException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!user.isEmailVerified()) {
            throw new DomainException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        return AuthResponse.bearer(jwtService.generateAccessToken(user.getEmail()), jwtService.accessTokenExpirationSeconds());
    }

    @Transactional
    public void forgotPassword(String email) {
        rateLimiter.check("password-reset", normalize(email), rateLimits.passwordReset());

        userRepository.findByEmail(normalize(email)).ifPresent(user -> {
            tokenRepository.deleteAllForUser(user.getId(), VerificationTokenType.PASSWORD_RESET);
            VerificationToken token = tokenRepository.save(
                    VerificationToken.issue(user, VerificationTokenType.PASSWORD_RESET));
            events.publishEvent(new PasswordResetRequested(user.getEmail(), token.getToken()));
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        VerificationToken verificationToken = useToken(request.token(), VerificationTokenType.PASSWORD_RESET);
        verificationToken.getUser().changePassword(passwordEncoder.encode(request.newPassword()));
    }

    private VerificationToken useToken(String token, VerificationTokenType type) {
        VerificationToken verificationToken = tokenRepository.findWithUser(token, type)
                .filter(found -> !found.isExpired())
                .orElseThrow(() -> new DomainException(ErrorCode.INVALID_TOKEN));
        tokenRepository.delete(verificationToken);
        return verificationToken;
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
