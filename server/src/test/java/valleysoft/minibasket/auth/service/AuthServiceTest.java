package valleysoft.minibasket.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import valleysoft.minibasket.auth.dto.AuthResponse;
import valleysoft.minibasket.auth.dto.LoginRequest;
import valleysoft.minibasket.auth.dto.RegisterRequest;
import valleysoft.minibasket.auth.entity.VerificationToken;
import valleysoft.minibasket.auth.event.EmailVerificationRequested;
import valleysoft.minibasket.auth.repository.VerificationTokenRepository;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.security.JwtProperties;
import valleysoft.minibasket.security.JwtService;
import valleysoft.minibasket.security.RateLimitProperties;
import valleysoft.minibasket.security.RateLimiter;
import valleysoft.minibasket.user.entity.Role;
import valleysoft.minibasket.user.entity.User;
import valleysoft.minibasket.user.mapper.UserMapperImpl;
import valleysoft.minibasket.user.repository.RoleRepository;
import valleysoft.minibasket.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final RateLimitProperties RATE_LIMITS = new RateLimitProperties(
            new RateLimitProperties.Limit(10, Duration.ofMinutes(15)), new RateLimitProperties.Limit(3, Duration.ofHours(1)));
    private static final String SECRET = "dGVzdC1vbmx5LXNlY3JldC1rZXktZm9yLW1pbmliYXNrZXQtdGVzdHM=";

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private ApplicationEventPublisher events;
    @Mock private RateLimiter rateLimiter;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, Duration.ofHours(1)));

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, tokenRepository, passwordEncoder, jwtService, events,
                new UserMapperImpl(), rateLimiter, RATE_LIMITS);
    }

    @Test
    void register_withNewEmail_savesCustomerAndSendsVerificationToken() {
        Role customerRole = mock(Role.class);
        when(customerRole.getName()).thenReturn(Role.CUSTOMER);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(new RegisterRequest("  New@Example.com ", "secret123"));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getEmail()).isEqualTo("new@example.com");
        assertThat(passwordEncoder.matches("secret123", savedUser.getValue().getPassword())).isTrue();
        assertThat(savedUser.getValue().isEmailVerified()).isFalse();
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
        ArgumentCaptor<EmailVerificationRequested> email = ArgumentCaptor.forClass(EmailVerificationRequested.class);
        verify(events).publishEvent(email.capture());
        assertThat(email.getValue().email()).isEqualTo("new@example.com");
        assertThat(email.getValue().token()).isNotBlank();
    }

    @Test
    void register_withTakenEmail_throwsConflict() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("taken@example.com", "secret123")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_TAKEN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        User user = verifiedUser("sara@example.com", "secret123");
        when(userRepository.findByEmail("sara@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("sara@example.com", "wrong-password")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_withUnknownEmail_throwsSameErrorAsWrongPassword() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "secret123")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_beforeEmailIsVerified_throwsEmailNotVerified() {
        User user = new User("sara@example.com", passwordEncoder.encode("secret123"), mock(Role.class));
        when(userRepository.findByEmail("sara@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("sara@example.com", "secret123")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    void login_withValidCredentials_returnsTokenForThatUser() {
        User user = verifiedUser("sara@example.com", "secret123");
        when(userRepository.findByEmail("sara@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(new LoginRequest("Sara@Example.com", "secret123"));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600);
        assertThat(jwtService.validateAndGetEmail(response.accessToken())).isEqualTo("sara@example.com");
    }

    private User verifiedUser(String email, String password) {
        User user = new User(email, passwordEncoder.encode(password), mock(Role.class));
        user.verifyEmail();
        return user;
    }
}
