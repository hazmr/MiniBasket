package valleysoft.minibasket.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import valleysoft.minibasket.auth.event.EmailVerificationRequested;
import valleysoft.minibasket.auth.event.PasswordResetRequested;
import valleysoft.minibasket.email.EmailMessage;
import valleysoft.minibasket.email.EmailProperties;
import valleysoft.minibasket.email.EmailSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEmailListener {

    private final EmailSender emailSender;
    private final EmailProperties properties;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendVerificationEmail(EmailVerificationRequested event) {
        try {
            String link = link("/verify-email", event.token());
            emailSender.send(new EmailMessage(event.email(), "Verify your MiniBasket email", "mail/verify-email",
                    Map.of("link", link), "Welcome to MiniBasket! Verify your email: " + link));
        } catch (MailException ex) {
            log.error("Could not send the verification email to {}: {}", event.email(), ex.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPasswordResetEmail(PasswordResetRequested event) {
        try {
            String link = link("/reset-password", event.token());
            emailSender.send(new EmailMessage(event.email(), "Reset your MiniBasket password", "mail/reset-password",
                    Map.of("link", link), "Choose a new MiniBasket password: " + link
                            + "\nIf you didn't ask for this, ignore this email."));
        } catch (MailException ex) {
            log.error("Could not send the password reset email to {}: {}", event.email(), ex.getMessage());
        }
    }

    private String link(String page, String token) {
        return properties.frontendUrl() + page + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
